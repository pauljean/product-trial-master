import { TestBed } from "@angular/core/testing";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { AuthService } from "./auth.service";
import { environment } from "../../../environments/environment";

describe("AuthService", () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should not be authenticated when no token in localStorage", () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it("register should POST to /api/account", () => {
    const request = { username: "u", firstname: "f", email: "e@e.com", password: "p" };
    service.register(request).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/account`);
    expect(req.request.method).toBe("POST");
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it("login should POST to /api/token and update state", () => {
    const request = { email: "admin@admin.com", password: "pass" };
    const response = { token: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBhZG1pbi5jb20ifQ.x" };
    service.login(request).subscribe((res) => {
      expect(res.token).toBe(response.token);
      expect(service.isAuthenticated()).toBe(true);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/token`);
    expect(req.request.method).toBe("POST");
    req.flush(response);
  });

  it("logout should clear token and set isAuthenticated to false", () => {
    service.login({ email: "a@a.com", password: "p" }).subscribe(() => {
      expect(service.isAuthenticated()).toBe(true);
      service.logout();
      expect(service.isAuthenticated()).toBe(false);
      expect(localStorage.getItem("token")).toBeNull();
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/token`);
    req.flush({ token: "jwt" });
  });

  it("getAuthHeaders should return Authorization header when token exists", () => {
    service.login({ email: "a@a.com", password: "p" }).subscribe(() => {
      expect(service.getAuthHeaders()).toEqual({ Authorization: "Bearer my-token" });
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/token`);
    req.flush({ token: "my-token" });
  });

  it("getAuthHeaders should return empty object when no token", () => {
    expect(service.getAuthHeaders()).toEqual({});
  });
});