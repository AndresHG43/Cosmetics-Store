package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.EntryCreateRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.EntryResponse;
import com.store.cosmetic.domain.dto.response.EntryStatusResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EntryService {
    EntryResponse create(EntryCreateRequest request);
    EntryResponse getById(Long id);
    PageResponse<EntryResponse> getAll(Pageable pageable);
    EntryResponse updateStatus(Long id, StatusUpdateRequest request);
    void delete(Long id);
    List<EntryStatusResponse> getAllStatuses();
    EntryStatusResponse getStatusById(Long id);
}
