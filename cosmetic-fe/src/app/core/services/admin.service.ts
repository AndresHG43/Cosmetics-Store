import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse, PageParams, User, UserCreateRequest, UserUpdateRequest, Role, UserRole } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly baseUrl = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) {}

  getAll(params?: PageParams): Observable<ApiResponse<PageResponse<User>>> {
    let httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20)
      .set('sort', params?.sort ?? 'id,desc');

    if (params?.search !== undefined && params?.search !== null && params.search.trim() !== '') {
      httpParams = httpParams.set('search', params.search.trim());
    }

    return this.http.get<ApiResponse<PageResponse<User>>>(this.baseUrl, { params: httpParams });
  }

  getById(id: number): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.baseUrl}/${id}`);
  }

  create(user: UserCreateRequest): Observable<ApiResponse<User>> {
    return this.http.post<ApiResponse<User>>(this.baseUrl, user);
  }

  update(id: number, user: UserUpdateRequest): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.baseUrl}/${id}`, user);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly baseUrl = `${environment.apiUrl}/role`;

  constructor(private http: HttpClient) {}

  getAll(params?: PageParams): Observable<ApiResponse<PageResponse<Role>>> {
    const httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20)
      .set('sort', params?.sort ?? 'id,desc');

    return this.http.get<ApiResponse<PageResponse<Role>>>(this.baseUrl, { params: httpParams });
  }
}

@Injectable({
  providedIn: 'root'
})
export class UserRoleService {
  private readonly baseUrl = `${environment.apiUrl}/users-role`;

  constructor(private http: HttpClient) {}

  getAll(params?: PageParams): Observable<ApiResponse<PageResponse<UserRole>>> {
    const httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20)
      .set('sort', params?.sort ?? 'id,desc');

    return this.http.get<ApiResponse<PageResponse<UserRole>>>(this.baseUrl, { params: httpParams });
  }
}
