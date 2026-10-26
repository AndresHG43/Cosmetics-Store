package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.UserCreateRequest;
import com.store.cosmetic.domain.dto.request.UserUpdateRequest;
import com.store.cosmetic.domain.dto.response.UserResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    UserResponse getById(Long id);
    PageResponse<UserResponse> getAll(String search, Pageable pageable);
    UserResponse update(Long id, UserUpdateRequest request);
    void delete(Long id);
}
