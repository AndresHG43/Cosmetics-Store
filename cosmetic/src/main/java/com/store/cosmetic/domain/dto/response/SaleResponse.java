package com.store.cosmetic.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private LocalDateTime date;
    private BigDecimal total;
    private SaleStatusResponse status;
    private Boolean active;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Long userCreated;
    private Long userUpdated;
    private List<SaleDetailResponse> details;
}
