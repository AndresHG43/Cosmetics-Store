package com.store.cosmetic.domain.mapper;

import com.store.cosmetic.domain.dto.response.SaleDetailResponse;
import com.store.cosmetic.domain.dto.response.SaleResponse;
import com.store.cosmetic.domain.dto.response.SaleStatusResponse;
import com.store.cosmetic.domain.model.Sale;
import com.store.cosmetic.domain.model.SaleDetail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaleMapper {

    public SaleStatusResponse toStatusResponse(com.store.cosmetic.domain.model.SaleStatus status) {
        if (status == null) return null;
        return SaleStatusResponse.builder()
                .id(status.getId())
                .status(status.getStatus())
                .dateCreated(status.getDateCreated())
                .build();
    }

    public SaleDetailResponse toDetailResponse(SaleDetail detail) {
        if (detail == null) return null;
        return SaleDetailResponse.builder()
                .saleId(detail.getId() != null ? detail.getId().getSaleId() : null)
                .productId(detail.getId() != null ? detail.getId().getProductId() : null)
                .productName(detail.getProduct() != null ? detail.getProduct().getName() : null)
                .priceSale(detail.getPriceSale())
                .quantity(detail.getQuantity())
                .subtotal(detail.getSubtotal())
                .active(detail.getActive())
                .build();
    }

    public SaleResponse toResponse(Sale sale, List<SaleDetail> details) {
        if (sale == null) return null;
        List<SaleDetailResponse> detailResponses = details == null ? List.of() :
                details.stream().map(this::toDetailResponse).toList();
        return SaleResponse.builder()
                .id(sale.getId())
                .date(sale.getDate())
                .total(sale.getTotal())
                .status(toStatusResponse(sale.getStatus()))
                .active(sale.getActive())
                .dateCreated(sale.getDateCreated())
                .dateUpdated(sale.getDateUpdated())
                .userCreated(sale.getUserCreated())
                .userUpdated(sale.getUserUpdated())
                .details(detailResponses)
                .build();
    }

    public SaleResponse toResponse(Sale sale) {
        return toResponse(sale, List.of());
    }
}
