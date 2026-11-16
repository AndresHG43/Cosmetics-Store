import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse, PageParams, Product, StockResponse } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly baseUrl = `${environment.apiUrl}/product`;

  constructor(private http: HttpClient) {}

  getAll(params?: PageParams): Observable<ApiResponse<PageResponse<Product>>> {
    let httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20)
      .set('sort', params?.sort ?? 'id,desc');

    if (params?.search !== undefined && params?.search !== null && params.search.trim() !== '') {
      httpParams = httpParams.set('search', params.search.trim());
    }

    if (params?.active !== undefined && params?.active !== null) {
      httpParams = httpParams.set('active', String(params.active));
    }

    return this.http.get<ApiResponse<PageResponse<Product>>>(this.baseUrl, { params: httpParams });
  }

  getStock(productId: number): Observable<ApiResponse<StockResponse>> {
    return this.http.get<ApiResponse<StockResponse>>(`${this.baseUrl}/${productId}/stock`);
  }

  getById(id: number): Observable<ApiResponse<Product>> {
    return this.http.get<ApiResponse<Product>>(`${this.baseUrl}/${id}`);
  }

  create(product: Partial<Product>): Observable<ApiResponse<Product>> {
    return this.http.post<ApiResponse<Product>>(this.baseUrl, product);
  }

  update(id: number, product: Partial<Product>): Observable<ApiResponse<Product>> {
    return this.http.put<ApiResponse<Product>>(`${this.baseUrl}/${id}`, product);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
