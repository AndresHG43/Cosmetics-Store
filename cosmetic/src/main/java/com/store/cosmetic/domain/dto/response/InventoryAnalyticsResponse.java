package com.store.cosmetic.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAnalyticsResponse {

    private Long totalEntries;
    private BigDecimal totalEntryExpenditure;
    private Map<String, Long> entryStatusCounts;
    private Map<String, Long> saleStatusCounts;
}
