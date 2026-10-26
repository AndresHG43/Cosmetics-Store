package com.store.cosmetic.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreateRequest {

    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDateTime date;

    @NotNull(message = "Status ID is required")
    private Long statusId;

    @NotEmpty(message = "Sale must have at least one detail item")
    @Valid
    private List<SaleDetailRequest> details;
}
