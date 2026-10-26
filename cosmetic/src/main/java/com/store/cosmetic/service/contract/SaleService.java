package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.SaleCreateRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.SaleResponse;
import com.store.cosmetic.domain.dto.response.SaleStatusResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SaleService {
    SaleResponse create(SaleCreateRequest request);
    SaleResponse getById(Long id);
    PageResponse<SaleResponse> getAll(Pageable pageable);
    SaleResponse updateStatus(Long id, StatusUpdateRequest request);
    void delete(Long id);
    List<SaleStatusResponse> getAllStatuses();
    SaleStatusResponse getStatusById(Long id);
}
