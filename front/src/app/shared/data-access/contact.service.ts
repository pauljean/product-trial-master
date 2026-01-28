import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";

export interface ContactRequest {
    email: string;
    message: string;
}

export interface ContactResponse {
    message: string;
}

@Injectable({
    providedIn: "root"
})
export class ContactService {
    private readonly http = inject(HttpClient);
    private readonly path = `${environment.apiUrl}/contact`;

    public sendContact(request: ContactRequest): Observable<ContactResponse> {
        return this.http.post<ContactResponse>(this.path, request);
    }
}
