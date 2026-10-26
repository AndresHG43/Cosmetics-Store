package com.store.cosmetic.service.implementation;

import com.store.cosmetic.domain.dto.response.InventoryAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.SalesAnalyticsResponse;
import com.store.cosmetic.domain.dto.response.TopProductResponse;
import com.store.cosmetic.repository.EntryRepository;
import com.store.cosmetic.repository.SaleDetailRepository;
import com.store.cosmetic.repository.SaleRepository;
import com.store.cosmetic.service.contract.AnalyticsService;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final EntryRepository entryRepository;

    public AnalyticsServiceImpl(SaleRepository saleRepository,
                                SaleDetailRepository saleDetailRepository,
                                EntryRepository entryRepository) {
        this.saleRepository = saleRepository;
        this.saleDetailRepository = saleDetailRepository;
        this.entryRepository = entryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getSalesAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Long count = saleRepository.countSalesBetween(startDate, endDate);
        if (count == null) {
            count = 0L;
        }

        BigDecimal revenue = saleRepository.sumRevenueBetween(startDate, endDate);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        BigDecimal avg = (count > 0)
                ? revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return SalesAnalyticsResponse.builder()
                .totalSalesCount(count)
                .grossRevenue(revenue)
                .averageSaleAmount(avg)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TopProductResponse> getTopProducts(Pageable pageable) {
        Page<Object[]> page = saleDetailRepository.findTopSellingProducts(pageable);
        Page<TopProductResponse> responsePage = page.map(row -> {
            Long productId = ((Number) row[0]).longValue();
            String productName = (String) row[1];
            Long quantitySold = ((Number) row[2]).longValue();
            BigDecimal revenue = (row[3] instanceof BigDecimal bd) ? bd : new BigDecimal(row[3].toString());
            return TopProductResponse.builder()
                    .productId(productId)
                    .productName(productName)
                    .totalQuantitySold(quantitySold)
                    .totalRevenue(revenue)
                    .build();
        });
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryAnalyticsResponse getInventoryAnalytics() {
        Long totalEntries = entryRepository.countTotalActiveEntries();
        if (totalEntries == null) {
            totalEntries = 0L;
        }

        BigDecimal expenditure = entryRepository.sumTotalActiveEntryExpenditure();
        if (expenditure == null) {
            expenditure = BigDecimal.ZERO;
        }

        Map<String, Long> entryStatusMap = new HashMap<>();
        for (Object[] row : entryRepository.countEntriesByStatus()) {
            entryStatusMap.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Long> saleStatusMap = new HashMap<>();
        for (Object[] row : saleRepository.countSalesByStatus()) {
            saleStatusMap.put((String) row[0], ((Number) row[1]).longValue());
        }

        return InventoryAnalyticsResponse.builder()
                .totalEntries(totalEntries)
                .totalEntryExpenditure(expenditure)
                .entryStatusCounts(entryStatusMap)
                .saleStatusCounts(saleStatusMap)
                .build();
    }
}
