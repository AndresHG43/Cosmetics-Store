package com.store.cosmetic.domain.mapper;

import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role) {
        if (role == null) return null;
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .dateCreated(role.getDateCreated())
                .dateUpdated(role.getDateUpdated())
                .userCreated(role.getUserCreated())
                .userUpdated(role.getUserUpdated())
                .build();
    }
}
