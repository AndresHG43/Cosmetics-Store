package com.store.cosmetic.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalyticsResponse {

    private Long totalSalesCount;
    private BigDecimal grossRevenue;
    private BigDecimal averageSaleAmount;
}
