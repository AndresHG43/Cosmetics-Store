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
public class EntryDetailResponse {
    private Long entryId;
    private Long productId;
    private String productName;
    private BigDecimal priceEntry;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean active;
}
