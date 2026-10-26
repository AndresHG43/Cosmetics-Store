package com.store.cosmetic.controller;

import com.store.cosmetic.config.ApiConfig;
import com.store.cosmetic.domain.dto.response.InventoryAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.SalesAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.TopProductResponse;
import com.store.cosmetic.service.contract.AnalyticsService;
import com.store.cosmetic.util.ApiResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping(value = "/sales", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SalesAnalyticsResponse>> getSalesAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SalesAnalyticsResponse response = analyticsService.getSalesAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping(value = "/top-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PageResponse<TopProductResponse>>> getTopProducts(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<TopProductResponse> response = analyticsService.getTopProducts(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping(value = "/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InventoryAnalyticsResponse>> getInventoryAnalytics() {
        InventoryAnalyticsResponse response = analyticsService.getInventoryAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
