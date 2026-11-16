import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse, PageParams, Entry, EntryCreateRequest, StatusUpdateRequest, StatusEntity } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class EntryService {
  private readonly baseUrl = `${environment.apiUrl}/entry`;
  private readonly statusUrl = `${environment.apiUrl}/entry-status`;

  constructor(private http: HttpClient) {}

  getAll(params?: PageParams): Observable<ApiResponse<PageResponse<Entry>>> {
    const httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20)
      .set('sort', params?.sort ?? 'id,desc');

    return this.http.get<ApiResponse<PageResponse<Entry>>>(this.baseUrl, { params: httpParams });
  }

  getStatuses(): Observable<ApiResponse<StatusEntity[]>> {
    return this.http.get<ApiResponse<StatusEntity[]>>(this.statusUrl);
  }

  getById(id: number): Observable<ApiResponse<Entry>> {
    return this.http.get<ApiResponse<Entry>>(`${this.baseUrl}/${id}`);
  }

  create(entry: EntryCreateRequest): Observable<ApiResponse<Entry>> {
    return this.http.post<ApiResponse<Entry>>(this.baseUrl, entry);
  }

  updateStatus(id: number, request: StatusUpdateRequest): Observable<ApiResponse<Entry>> {
    return this.http.put<ApiResponse<Entry>>(`${this.baseUrl}/${id}/status`, request);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
