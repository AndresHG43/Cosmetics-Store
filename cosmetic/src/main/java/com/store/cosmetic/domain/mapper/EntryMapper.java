package com.store.cosmetic.domain.mapper;

import com.store.cosmetic.domain.dto.response.EntryDetailResponse;
import com.store.cosmetic.domain.dto.response.EntryResponse;
import com.store.cosmetic.domain.dto.response.EntryStatusResponse;
import com.store.cosmetic.domain.model.Entry;
import com.store.cosmetic.domain.model.EntryDetail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntryMapper {

    public EntryStatusResponse toStatusResponse(com.store.cosmetic.domain.model.EntryStatus status) {
        if (status == null) return null;
        return EntryStatusResponse.builder()
                .id(status.getId())
                .status(status.getStatus())
                .dateCreated(status.getDateCreated())
                .build();
    }

    public EntryDetailResponse toDetailResponse(EntryDetail detail) {
        if (detail == null) return null;
        return EntryDetailResponse.builder()
                .entryId(detail.getId() != null ? detail.getId().getEntryId() : null)
                .productId(detail.getId() != null ? detail.getId().getProductId() : null)
                .productName(detail.getProduct() != null ? detail.getProduct().getName() : null)
                .priceEntry(detail.getPriceEntry())
                .quantity(detail.getQuantity())
                .subtotal(detail.getSubtotal())
                .active(detail.getActive())
                .build();
    }

    public EntryResponse toResponse(Entry entry, List<EntryDetail> details) {
        if (entry == null) return null;
        List<EntryDetailResponse> detailResponses = details == null ? List.of() :
                details.stream().map(this::toDetailResponse).toList();
        return EntryResponse.builder()
                .id(entry.getId())
                .date(entry.getDate())
                .total(entry.getTotal())
                .isInitial(entry.getIsInitial())
                .status(toStatusResponse(entry.getStatus()))
                .active(entry.getActive())
                .dateCreated(entry.getDateCreated())
                .dateUpdated(entry.getDateUpdated())
                .userCreated(entry.getUserCreated())
                .userUpdated(entry.getUserUpdated())
                .details(detailResponses)
                .build();
    }

    public EntryResponse toResponse(Entry entry) {
        return toResponse(entry, List.of());
    }
}
