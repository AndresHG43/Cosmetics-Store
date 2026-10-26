package com.store.cosmetic.domain.mapper;

import com.store.cosmetic.domain.dto.response.UserRoleResponse;
import com.store.cosmetic.domain.model.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRoleResponse toResponse(UserRole userRole) {
        if (userRole == null) return null;
        return UserRoleResponse.builder()
                .id(userRole.getId())
                .userId(userRole.getUser() != null ? userRole.getUser().getId() : null)
                .userName(userRole.getUser() != null ? userRole.getUser().getName() + " " + userRole.getUser().getLastname() : null)
                .roleId(userRole.getRole() != null ? userRole.getRole().getId() : null)
                .roleName(userRole.getRole() != null ? userRole.getRole().getName() : null)
                .active(userRole.getActive())
                .dateCreated(userRole.getDateCreated())
                .userCreated(userRole.getUserCreated())
                .build();
    }
}
