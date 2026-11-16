import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse, PageParams, InventoryAnalytics, TopProduct, SalesAnalytics } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private readonly baseUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  getInventory(): Observable<ApiResponse<InventoryAnalytics>> {
    return this.http.get<ApiResponse<InventoryAnalytics>>(`${this.baseUrl}/inventory`);
  }

  getTopProducts(params?: PageParams): Observable<ApiResponse<PageResponse<TopProduct>>> {
    const httpParams = new HttpParams()
      .set('page', params?.page ?? 0)
      .set('size', params?.size ?? 20);

    return this.http.get<ApiResponse<PageResponse<TopProduct>>>(`${this.baseUrl}/top-products`, { params: httpParams });
  }

  getSales(startDate?: string, endDate?: string): Observable<ApiResponse<SalesAnalytics>> {
    let httpParams = new HttpParams();
    if (startDate) {
      httpParams = httpParams.set('startDate', startDate);
    }
    if (endDate) {
      httpParams = httpParams.set('endDate', endDate);
    }

    return this.http.get<ApiResponse<SalesAnalytics>>(`${this.baseUrl}/sales`, { params: httpParams });
  }
}
