package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.RoleCreateRequest;
import com.store.cosmetic.domain.dto.request.RoleUpdateRequest;
import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

public interface RoleService {
    RoleResponse create(RoleCreateRequest request);
    RoleResponse getById(Long id);
    PageResponse<RoleResponse> getAll(Pageable pageable);
    RoleResponse update(Long id, RoleUpdateRequest request);
    void delete(Long id);
}
