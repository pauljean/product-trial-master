import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, tap } from "rxjs";
import { environment } from "../../../environments/environment";

export interface RegisterRequest {
    username: string;
    firstname: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
}

@Injectable({
    providedIn: "root"
})
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly path = `${environment.apiUrl}`;
    
    private readonly _token = signal<string | null>(null);
    private readonly _isAuthenticated = signal<boolean>(false);

    public readonly token = this._token.asReadonly();
    public readonly isAuthenticated = this._isAuthenticated.asReadonly();

    constructor() {
        const savedToken = localStorage.getItem('token');
        if (savedToken) {
            this._token.set(savedToken);
            this._isAuthenticated.set(true);
        }
    }

    public register(request: RegisterRequest): Observable<any> {
        return this.http.post(`${this.path}/account`, request);
    }

    public login(request: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.path}/token`, request).pipe(
            tap((response) => {
                this._token.set(response.token);
                this._isAuthenticated.set(true);
                localStorage.setItem('token', response.token);
            })
        );
    }

    public logout(): void {
        this._token.set(null);
        this._isAuthenticated.set(false);
        localStorage.removeItem('token');
    }

    public getAuthHeaders(): { [key: string]: string } {
        const token = this._token();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    public getCurrentUserEmail(): string | null {
        const token = this._token();
        if (!token) return null;
        
        try {
            // Décoder le JWT (base64)
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.sub || null; // 'sub' contient l'email dans notre JWT
        } catch (e) {
            return null;
        }
    }
}
