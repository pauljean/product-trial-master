import { Injectable, inject, signal, computed, effect } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, tap, of, catchError } from "rxjs";
import { environment } from "../../../environments/environment";
import { AuthService } from "./auth.service";
import { ProductsService } from "app/products/data-access/products.service";
import { Product } from "app/products/data-access/product.model";

export interface CartItem {
    id: number;
    product: {
        id: number;
        code: string;
        name: string;
        description: string;
        image: string;
        category: string;
        price: number;
        quantity: number;
        inventoryStatus: string;
        rating: number;
    };
    quantity: number;
}

interface LocalCartItem {
    productId: number;
    quantity: number;
}

@Injectable({
    providedIn: "root"
})
export class CartService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);
    private readonly productsService = inject(ProductsService);
    private readonly path = `${environment.apiUrl}/cart`;
    private readonly LOCAL_CART_KEY = 'localCart';
    
    private readonly _cartItems = signal<CartItem[]>([]);
    private nextLocalId = 10000; // Pour les IDs locaux

    public readonly cartItems = this._cartItems.asReadonly();
    public readonly cartItemCount = computed(() => 
        this._cartItems().reduce((sum, item) => sum + item.quantity, 0)
    );

    constructor() {
        // Charger le panier local au démarrage
        this.loadLocalCart();
        
        // Synchroniser avec le serveur quand l'utilisateur se connecte
        effect(() => {
            if (this.authService.isAuthenticated()) {
                this.syncLocalCartToServer();
            } else {
                // Si déconnecté, charger le panier local
                this.loadLocalCart();
            }
        });
    }

    private loadLocalCart(): void {
        if (!this.authService.isAuthenticated()) {
            const localCart = this.getLocalCart();
            const products = this.productsService.products() as Product[];
            
            // Si les produits ne sont pas encore chargés, charger d'abord
            if (products.length === 0) {
                this.productsService.get().subscribe(() => {
                    this.loadLocalCart();
                });
                return;
            }
            
            const cartItems: CartItem[] = localCart.map(item => {
                const product = products.find((p: Product) => p.id === item.productId);
                if (!product) return null;
                
                return {
                    id: this.nextLocalId++,
                    product: {
                        id: product.id,
                        code: product.code,
                        name: product.name,
                        description: product.description,
                        image: product.image,
                        category: product.category,
                        price: product.price,
                        quantity: product.quantity,
                        inventoryStatus: product.inventoryStatus,
                        rating: product.rating
                    },
                    quantity: item.quantity
                };
            }).filter(item => item !== null) as CartItem[];
            
            this._cartItems.set(cartItems);
        }
    }

    private getLocalCart(): LocalCartItem[] {
        const stored = localStorage.getItem(this.LOCAL_CART_KEY);
        return stored ? JSON.parse(stored) : [];
    }

    private saveLocalCart(items: LocalCartItem[]): void {
        localStorage.setItem(this.LOCAL_CART_KEY, JSON.stringify(items));
    }

    private syncLocalCartToServer(): void {
        const localCart = this.getLocalCart();
        if (localCart.length === 0) {
            // Charger le panier du serveur
            this.getCartItems().subscribe();
            return;
        }

        // Synchroniser chaque article local avec le serveur
        const headers = this.authService.getAuthHeaders();
        localCart.forEach(item => {
            this.http.post<CartItem>(`${this.path}/add`, { 
                productId: item.productId, 
                quantity: item.quantity 
            }, { headers }).subscribe();
        });

        // Vider le panier local et charger depuis le serveur
        localStorage.removeItem(this.LOCAL_CART_KEY);
        setTimeout(() => this.getCartItems().subscribe(), 500);
    }

    public getCartItems(): Observable<CartItem[]> {
        if (!this.authService.isAuthenticated()) {
            this.loadLocalCart();
            return of(this._cartItems());
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.get<CartItem[]>(this.path, { headers }).pipe(
            tap((items) => this._cartItems.set(items)),
            catchError(() => {
                this.loadLocalCart();
                return of(this._cartItems());
            })
        );
    }

    public addToCart(productId: number, quantity: number = 1): Observable<CartItem | null> {
        if (!this.authService.isAuthenticated()) {
            // Ajouter au panier local
            const localCart = this.getLocalCart();
            const existingIndex = localCart.findIndex(item => item.productId === productId);
            
            if (existingIndex >= 0) {
                localCart[existingIndex].quantity += quantity;
            } else {
                localCart.push({ productId, quantity });
            }
            
            this.saveLocalCart(localCart);
            this.loadLocalCart();
            
            const products = this.productsService.products() as Product[];
            const product = products.find((p: Product) => p.id === productId);
            if (!product) return of(null);
            
            const cartItem: CartItem = {
                id: this.nextLocalId++,
                product: {
                    id: product.id,
                    code: product.code,
                    name: product.name,
                    description: product.description,
                    image: product.image,
                    category: product.category,
                    price: product.price,
                    quantity: product.quantity,
                    inventoryStatus: product.inventoryStatus,
                    rating: product.rating
                },
                quantity: localCart.find(item => item.productId === productId)?.quantity || quantity
            };
            
            return of(cartItem);
        }
        
        // Utilisateur connecté : utiliser le serveur
        const headers = this.authService.getAuthHeaders();
        return this.http.post<CartItem>(`${this.path}/add`, { productId, quantity }, { headers }).pipe(
            tap(() => this.getCartItems().subscribe())
        );
    }

    public updateCartItemQuantity(cartItemId: number, quantity: number): Observable<CartItem | null> {
        if (!this.authService.isAuthenticated()) {
            // Mettre à jour le panier local
            const cartItem = this._cartItems().find(item => item.id === cartItemId);
            if (!cartItem) return of(null);
            
            const localCart = this.getLocalCart();
            const itemIndex = localCart.findIndex(item => item.productId === cartItem.product.id);
            
            if (itemIndex >= 0) {
                localCart[itemIndex].quantity = quantity;
                this.saveLocalCart(localCart);
                this.loadLocalCart();
            }
            
            return of(cartItem);
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.patch<CartItem>(`${this.path}/${cartItemId}`, { quantity }, { headers }).pipe(
            tap(() => this.getCartItems().subscribe())
        );
    }

    public removeFromCart(cartItemId: number): Observable<void> {
        if (!this.authService.isAuthenticated()) {
            // Retirer du panier local
            const cartItem = this._cartItems().find(item => item.id === cartItemId);
            if (cartItem) {
                const localCart = this.getLocalCart();
                const filtered = localCart.filter(item => item.productId !== cartItem.product.id);
                this.saveLocalCart(filtered);
                this.loadLocalCart();
            }
            return of(undefined);
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.delete<void>(`${this.path}/${cartItemId}`, { headers }).pipe(
            tap(() => this.getCartItems().subscribe())
        );
    }

    public clearCart(): Observable<void> {
        if (!this.authService.isAuthenticated()) {
            localStorage.removeItem(this.LOCAL_CART_KEY);
            this._cartItems.set([]);
            return of(undefined);
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.delete<void>(this.path, { headers }).pipe(
            tap(() => {
                this._cartItems.set([]);
                localStorage.removeItem(this.LOCAL_CART_KEY);
            })
        );
    }
}
