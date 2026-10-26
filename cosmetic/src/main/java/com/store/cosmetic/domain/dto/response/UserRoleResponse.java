package com.store.cosmetic.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long roleId;
    private String roleName;
    private Boolean active;
    private LocalDateTime dateCreated;
    private Long userCreated;
}
