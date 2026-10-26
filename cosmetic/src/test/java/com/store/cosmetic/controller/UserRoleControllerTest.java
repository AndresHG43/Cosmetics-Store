package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.UserRoleCreateRequest;
import com.store.cosmetic.domain.dto.response.UserRoleResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.service.contract.UserRoleService;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserRoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRoleService userRoleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserRoleResponse sampleResponse;

    @BeforeEach
    void setUp() {
        UserRoleController controller = new UserRoleController(userRoleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = UserRoleResponse.builder()
                .id(1L)
                .userId(10L)
                .roleId(2L)
                .roleName("ADMIN")
                .active(true)
                .build();
    }

    @Test
    void testAssignRole_Success() throws Exception {
        UserRoleCreateRequest request = UserRoleCreateRequest.builder()
                .userId(10L)
                .roleId(2L)
                .build();

        when(userRoleService.assign(any(UserRoleCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/cosmetic/api/users-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("ADMIN"));
    }

    @Test
    void testGetById_Success() throws Exception {
        when(userRoleService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/cosmetic/api/users-roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetAll_Success() throws Exception {
        PageResponse<UserRoleResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1)
        );
        when(userRoleService.getAll(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/users-roles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].roleName").value("ADMIN"));
    }

    @Test
    void testRevokeRole_Success() throws Exception {
        mockMvc.perform(delete("/cosmetic/api/users-roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
