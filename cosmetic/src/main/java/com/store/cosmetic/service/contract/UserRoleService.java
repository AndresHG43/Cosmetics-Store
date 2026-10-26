package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.UserRoleCreateRequest;
import com.store.cosmetic.domain.dto.response.UserRoleResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

public interface UserRoleService {
    UserRoleResponse assign(UserRoleCreateRequest request);
    UserRoleResponse getById(Long id);
    PageResponse<UserRoleResponse> getAll(Pageable pageable);
    void revoke(Long id);
}
