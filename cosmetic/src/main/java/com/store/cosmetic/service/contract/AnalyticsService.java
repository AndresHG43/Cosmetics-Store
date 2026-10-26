package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.response.InventoryAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.SalesAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.TopProductResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AnalyticsService {

    SalesAnalyticsResponse getSalesAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    PageResponse<TopProductResponse> getTopProducts(Pageable pageable);

    InventoryAnalyticsResponse getInventoryAnalytics();
}
