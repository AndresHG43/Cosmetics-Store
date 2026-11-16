import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthLoginRequest, AuthResponse, User } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'cosmetic_token';
  private readonly REFRESH_TOKEN_KEY = 'cosmetic_refresh_token';
  private readonly USER_KEY = 'cosmetic_user';

  // Signals for reactive state
  private tokenSignal = signal<string | null>(this.getStoredToken());
  private currentUserSignal = signal<User | null>(this.getStoredUser());

  readonly isAuthenticated = computed(() => !!this.tokenSignal());
  readonly currentUser = computed(() => this.currentUserSignal());
  readonly userRoles = computed(() => this.currentUserSignal()?.roles?.map(r => r.name) || []);
  readonly isAdmin = computed(() => {
    const roles = this.userRoles();
    return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_ROOT') || roles.includes('ADMIN') || roles.includes('ROOT');
  });

  constructor(private http: HttpClient) {}

  login(credentials: AuthLoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setSession(response.data);
        }
      })
    );
  }

  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    const refreshToken = this.getStoredRefreshToken();
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setSession(response.data);
        }
      })
    );
  }

  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
    this.tokenSignal.set(null);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  private setSession(auth: AuthResponse): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem(this.TOKEN_KEY, auth.token);
      if (auth.refreshToken) {
        localStorage.setItem(this.REFRESH_TOKEN_KEY, auth.refreshToken);
      }
      const user: User = {
        email: auth.email,
        roles: (auth.roles || []).map(name => ({ name }))
      };
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.currentUserSignal.set(user);
    }
    this.tokenSignal.set(auth.token);
  }

  private getStoredToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  private getStoredRefreshToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    }
    return null;
  }

  private getStoredUser(): User | null {
    if (typeof window !== 'undefined') {
      const userJson = localStorage.getItem(this.USER_KEY);
      if (userJson) {
        try {
          return JSON.parse(userJson);
        } catch {
          return null;
        }
      }
    }
    return null;
  }
}
