package com.store.cosmetic.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private Boolean active;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private List<RoleResponse> roles;
}
