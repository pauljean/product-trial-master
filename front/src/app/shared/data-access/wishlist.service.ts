import { Injectable, inject, signal, computed } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, tap, of, catchError } from "rxjs";
import { environment } from "../../../environments/environment";
import { AuthService } from "./auth.service";
import { ProductsService } from "../../products/data-access/products.service";
import { Product } from "../../products/data-access/product.model";

export interface WishlistItem {
    id: number;
    product: Product;
}

@Injectable({
    providedIn: "root"
})
export class WishlistService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);
    private readonly productsService = inject(ProductsService);
    private readonly path = `${environment.apiUrl}/wishlist`;
    
    private readonly _wishlistItems = signal<WishlistItem[]>([]);
    private nextLocalId = 20000;

    public readonly wishlistItems = this._wishlistItems.asReadonly();
    public readonly wishlistItemCount = computed(() => this._wishlistItems().length);

    constructor() {
        this.loadLocalWishlist();
        
        // Synchroniser avec le serveur quand l'utilisateur se connecte
        if (this.authService.isAuthenticated()) {
            this.syncLocalWishlistToServer();
        }
    }

    private loadLocalWishlist(): void {
        if (!this.authService.isAuthenticated()) {
            const localWishlist = this.getLocalWishlist();
            const products = this.productsService.products() as Product[];
            
            if (products.length === 0) {
                this.productsService.get().subscribe(() => {
                    this.loadLocalWishlist();
                });
                return;
            }
            
            const wishlistItems: WishlistItem[] = localWishlist.map(productId => {
                const product = products.find((p: Product) => p.id === productId);
                if (!product) return null;
                
                return {
                    id: this.nextLocalId++,
                    product: product
                };
            }).filter(item => item !== null) as WishlistItem[];
            
            this._wishlistItems.set(wishlistItems);
        }
    }

    private getLocalWishlist(): number[] {
        const stored = localStorage.getItem('localWishlist');
        return stored ? JSON.parse(stored) : [];
    }

    private saveLocalWishlist(productIds: number[]): void {
        localStorage.setItem('localWishlist', JSON.stringify(productIds));
    }

    private syncLocalWishlistToServer(): void {
        const localWishlist = this.getLocalWishlist();
        if (localWishlist.length === 0) {
            this.getWishlistItems().subscribe();
            return;
        }

        const headers = this.authService.getAuthHeaders();
        localWishlist.forEach(productId => {
            this.http.post<WishlistItem>(`${this.path}/add`, { 
                productId: productId
            }, { headers }).subscribe();
        });

        localStorage.removeItem('localWishlist');
        setTimeout(() => this.getWishlistItems().subscribe(), 500);
    }

    public getWishlistItems(): Observable<WishlistItem[]> {
        if (!this.authService.isAuthenticated()) {
            this.loadLocalWishlist();
            return of(this._wishlistItems());
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.get<WishlistItem[]>(this.path, { headers }).pipe(
            tap((items) => this._wishlistItems.set(items)),
            catchError(() => {
                this.loadLocalWishlist();
                return of(this._wishlistItems());
            })
        );
    }

    public addToWishlist(productId: number): Observable<WishlistItem | null> {
        if (!this.authService.isAuthenticated()) {
            const localWishlist = this.getLocalWishlist();
            if (!localWishlist.includes(productId)) {
                localWishlist.push(productId);
                this.saveLocalWishlist(localWishlist);
                this.loadLocalWishlist();
            }
            
            const products = this.productsService.products() as Product[];
            const product = products.find((p: Product) => p.id === productId);
            if (!product) return of(null);
            
            return of({
                id: this.nextLocalId++,
                product: product
            });
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.post<WishlistItem>(`${this.path}/add`, { productId }, { headers }).pipe(
            tap(() => this.getWishlistItems().subscribe())
        );
    }

    public removeFromWishlist(wishlistItemId: number, productId?: number): Observable<void> {
        if (!this.authService.isAuthenticated()) {
            if (productId) {
                const localWishlist = this.getLocalWishlist();
                const filtered = localWishlist.filter(id => id !== productId);
                this.saveLocalWishlist(filtered);
                this.loadLocalWishlist();
            }
            return of(undefined);
        }
        
        const headers = this.authService.getAuthHeaders();
        return this.http.delete<void>(`${this.path}/${wishlistItemId}`, { headers }).pipe(
            tap(() => this.getWishlistItems().subscribe())
        );
    }

    public isInWishlist(productId: number): boolean {
        return this._wishlistItems().some(item => item.product.id === productId);
    }
}
