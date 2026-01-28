import { Injectable, inject, signal } from "@angular/core";
import { Product } from "./product.model";
import { HttpClient, HttpParams } from "@angular/common/http";
import { catchError, Observable, of, tap } from "rxjs";
import { environment } from "../../../environments/environment";
import { AuthService } from "../../shared/data-access/auth.service";

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

@Injectable({
    providedIn: "root"
}) export class ProductsService {

    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);
    private readonly path = `${environment.apiUrl}/products`;
    
    private readonly _products = signal<Product[]>([]);

    public readonly products = this._products.asReadonly();

    public get(params?: {
        page?: number;
        size?: number;
        sortBy?: string;
        sortDir?: 'ASC' | 'DESC';
        category?: string;
        search?: string;
    }): Observable<Product[] | PageResponse<Product>> {
        let httpParams = new HttpParams();
        
        if (params) {
            if (params.page !== undefined) httpParams = httpParams.set('page', params.page);
            if (params.size !== undefined) httpParams = httpParams.set('size', params.size);
            if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
            if (params.sortDir) httpParams = httpParams.set('sortDir', params.sortDir);
            if (params.category) httpParams = httpParams.set('category', params.category);
            if (params.search) httpParams = httpParams.set('search', params.search);
        }
        
        return this.http.get<Product[] | PageResponse<Product>>(this.path, { params: httpParams }).pipe(
            catchError((error) => {
                console.error('Erreur lors de la récupération des produits:', error);
                return this.http.get<Product[]>("assets/products.json");
            }),
            tap((response) => {
                if (Array.isArray(response)) {
                    this._products.set(response);
                } else {
                    // Si c'est une PageResponse, extraire le content
                    this._products.set(response.content);
                }
            }),
        );
    }
    
    public getById(id: number): Observable<Product> {
        return this.http.get<Product>(`${this.path}/${id}`);
    }

    public create(product: Product): Observable<Product> {
        const headers = this.authService.getAuthHeaders();
        return this.http.post<Product>(this.path, product, { headers }).pipe(
            catchError(() => {
                return of(product);
            }),
            tap((createdProduct) => this._products.update(products => [createdProduct, ...products])),
        );
    }

    public update(product: Product): Observable<Product> {
        const headers = this.authService.getAuthHeaders();
        return this.http.patch<Product>(`${this.path}/${product.id}`, product, { headers }).pipe(
            catchError(() => {
                return of(product);
            }),
            tap((updatedProduct) => this._products.update(products => {
                return products.map(p => p.id === updatedProduct.id ? updatedProduct : p)
            })),
        );
    }

    public delete(productId: number): Observable<void> {
        const headers = this.authService.getAuthHeaders();
        return this.http.delete<void>(`${this.path}/${productId}`, { headers }).pipe(
            catchError(() => {
                return of(undefined);
            }),
            tap(() => this._products.update(products => products.filter(product => product.id !== productId))),
        );
    }
}
