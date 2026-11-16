import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse, PageParams, Sale, SaleCreateRequest, StatusUpdateRequest, StatusEntity } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class SaleService {
  private readonly baseUrl = `${environment.apiUrl}/sale`;
  private readonly statusUrl = `${environment.apiUrl}/sale-status`;

  constructor(private http: HttpClient) {}

  getAll(params?: PageParams): Observable<ApiResponse<PageResponse<Sale>>> {
    const httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20)
      .set('sort', params?.sort ?? 'id,desc');

    return this.http.get<ApiResponse<PageResponse<Sale>>>(this.baseUrl, { params: httpParams });
  }

  getStatuses(): Observable<ApiResponse<StatusEntity[]>> {
    return this.http.get<ApiResponse<StatusEntity[]>>(this.statusUrl);
  }

  getById(id: number): Observable<ApiResponse<Sale>> {
    return this.http.get<ApiResponse<Sale>>(`${this.baseUrl}/${id}`);
  }

  create(sale: SaleCreateRequest): Observable<ApiResponse<Sale>> {
    return this.http.post<ApiResponse<Sale>>(this.baseUrl, sale);
  }

  updateStatus(id: number, request: StatusUpdateRequest): Observable<ApiResponse<Sale>> {
    return this.http.put<ApiResponse<Sale>>(`${this.baseUrl}/${id}/status`, request);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
