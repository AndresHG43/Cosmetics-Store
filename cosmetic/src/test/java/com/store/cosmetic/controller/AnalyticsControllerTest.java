package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.response.InventoryAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.SalesAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.TopProductResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.service.contract.AnalyticsService;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        AnalyticsController controller = new AnalyticsController(analyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testGetSalesAnalytics() throws Exception {
        SalesAnalyticsResponse response = SalesAnalyticsResponse.builder()
                .totalSalesCount(25L)
                .grossRevenue(new BigDecimal("1250.00"))
                .averageSaleAmount(new BigDecimal("50.00"))
                .build();

        when(analyticsService.getSalesAnalytics(any(), any())).thenReturn(response);

        mockMvc.perform(get("/cosmetic/api/analytics/sales")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSalesCount").value(25))
                .andExpect(jsonPath("$.data.grossRevenue").value(1250.00))
                .andExpect(jsonPath("$.data.averageSaleAmount").value(50.00));
    }

    @Test
    void testGetTopProducts() throws Exception {
        TopProductResponse item = TopProductResponse.builder()
                .productId(1L)
                .productName("Lipstick Red")
                .totalQuantitySold(100L)
                .totalRevenue(new BigDecimal("1500.00"))
                .build();

        PageResponse<TopProductResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1)
        );

        when(analyticsService.getTopProducts(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/analytics/top-products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].productId").value(1))
                .andExpect(jsonPath("$.data.content[0].productName").value("Lipstick Red"))
                .andExpect(jsonPath("$.data.content[0].totalQuantitySold").value(100));
    }

    @Test
    void testGetInventoryAnalytics() throws Exception {
        InventoryAnalyticsResponse response = InventoryAnalyticsResponse.builder()
                .totalEntries(18L)
                .totalEntryExpenditure(new BigDecimal("4500.00"))
                .entryStatusCounts(Map.of("DONE", 15L, "IN PROGRESS", 3L))
                .saleStatusCounts(Map.of("DONE", 30L))
                .build();

        when(analyticsService.getInventoryAnalytics()).thenReturn(response);

        mockMvc.perform(get("/cosmetic/api/analytics/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEntries").value(18))
                .andExpect(jsonPath("$.data.totalEntryExpenditure").value(4500.00))
                .andExpect(jsonPath("$.data.entryStatusCounts.DONE").value(15));
    }
}
