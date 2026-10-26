package com.store.cosmetic.domain.mapper;

import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.domain.dto.response.UserResponse;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserResponse toResponse(User user, List<UserRole> userRoles) {
        if (user == null) return null;
        List<RoleResponse> roles = userRoles == null ? List.of() :
                userRoles.stream()
                        .filter(ur -> Boolean.TRUE.equals(ur.getActive()))
                        .map(ur -> roleMapper.toResponse(ur.getRole()))
                        .toList();
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .active(user.getActive())
                .dateCreated(user.getDateCreated())
                .dateUpdated(user.getDateUpdated())
                .roles(roles)
                .build();
    }

    public UserResponse toResponse(User user) {
        return toResponse(user, List.of());
    }
}
