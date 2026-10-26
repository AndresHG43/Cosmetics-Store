package com.store.cosmetic.controller;

import com.store.cosmetic.config.ApiConfig;
import com.store.cosmetic.domain.dto.request.UserRoleCreateRequest;
import com.store.cosmetic.domain.dto.response.UserRoleResponse;
import com.store.cosmetic.service.contract.UserRoleService;
import com.store.cosmetic.util.ApiResponse;
import com.store.cosmetic.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/users-role")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserRoleResponse>> assign(@Valid @RequestBody UserRoleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Role assigned successfully", userRoleService.assign(request)));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserRoleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userRoleService.getById(id)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PageResponse<UserRoleResponse>>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(userRoleService.getAll(pageable)));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable Long id) {
        userRoleService.revoke(id);
        return ResponseEntity.ok(ApiResponse.message("Role revoked successfully"));
    }
}
