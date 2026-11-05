package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.response.InventoryAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.SalesAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.TopProductResponse;
import com.store.cosmetic.repository.EntryRepository;
import com.store.cosmetic.repository.SaleDetailRepository;
import com.store.cosmetic.repository.SaleRepository;
import com.store.cosmetic.service.implementation.AnalyticsServiceImpl;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleDetailRepository saleDetailRepository;

    @Mock
    private EntryRepository entryRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void testGetSalesAnalytics_WithSales() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(saleRepository.countSalesBetween(start, end)).thenReturn(10L);
        when(saleRepository.sumRevenueBetween(start, end)).thenReturn(new BigDecimal("250.00"));

        SalesAnalyticsResponse response = analyticsService.getSalesAnalytics(start, end);

        assertNotNull(response);
        assertEquals(10L, response.getTotalSalesCount());
        assertEquals(new BigDecimal("250.00"), response.getGrossRevenue());
        assertEquals(new BigDecimal("25.00"), response.getAverageSaleAmount());
    }

    @Test
    void testGetSalesAnalytics_NoSales() {
        when(saleRepository.countSalesBetween(null, null)).thenReturn(0L);
        when(saleRepository.sumRevenueBetween(null, null)).thenReturn(BigDecimal.ZERO);

        SalesAnalyticsResponse response = analyticsService.getSalesAnalytics(null, null);

        assertNotNull(response);
        assertEquals(0L, response.getTotalSalesCount());
        assertEquals(BigDecimal.ZERO, response.getGrossRevenue());
        assertEquals(BigDecimal.ZERO, response.getAverageSaleAmount());
    }

    @Test
    void testGetTopProducts() {
        Pageable pageable = PageRequest.of(0, 5);
        Object[] row1 = new Object[]{1L, "Lipstick Red", 50L, new BigDecimal("750.00")};
        Object[] row2 = new Object[]{2L, "Facial Serum", 30L, new BigDecimal("600.00")};

        PageImpl<Object[]> page = new PageImpl<>(List.of(row1, row2), pageable, 2);
        when(saleDetailRepository.findTopSellingProducts(pageable)).thenReturn(page);

        PageResponse<TopProductResponse> response = analyticsService.getTopProducts(pageable);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(1L, response.getContent().get(0).getProductId());
        assertEquals("Lipstick Red", response.getContent().get(0).getProductName());
        assertEquals(50L, response.getContent().get(0).getTotalQuantitySold());
        assertEquals(new BigDecimal("750.00"), response.getContent().get(0).getTotalRevenue());
    }

    @Test
    void testGetInventoryAnalytics() {
        when(entryRepository.countTotalActiveEntries()).thenReturn(15L);
        when(entryRepository.sumTotalActiveEntryExpenditure()).thenReturn(new BigDecimal("3200.50"));

        List<Object[]> entryStatuses = List.of(new Object[]{"DONE", 12L}, new Object[]{"IN PROGRESS", 3L});
        when(entryRepository.countEntriesByStatus()).thenReturn(entryStatuses);

        List<Object[]> saleStatuses = List.of(new Object[]{"DONE", 20L}, new Object[]{"PENDING DELIVERY", 5L});
        when(saleRepository.countSalesByStatus()).thenReturn(saleStatuses);

        InventoryAnalyticsResponse response = analyticsService.getInventoryAnalytics();

        assertNotNull(response);
        assertEquals(15L, response.getTotalEntries());
        assertEquals(new BigDecimal("3200.50"), response.getTotalEntryExpenditure());
        assertEquals(12L, response.getEntryStatusCounts().get("DONE"));
        assertEquals(3L, response.getEntryStatusCounts().get("IN PROGRESS"));
        assertEquals(20L, response.getSaleStatusCounts().get("DONE"));
        assertEquals(5L, response.getSaleStatusCounts().get("PENDING DELIVERY"));
    }
}
