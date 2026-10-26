package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.RoleCreateRequest;
import com.store.cosmetic.domain.dto.request.RoleUpdateRequest;
import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.service.contract.RoleService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RoleResponse sampleRoleResponse;

    @BeforeEach
    void setUp() {
        RoleController controller = new RoleController(roleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleRoleResponse = RoleResponse.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator role")
                .active(true)
                .build();
    }

    @Test
    void testCreateRole_Success() throws Exception {
        RoleCreateRequest request = RoleCreateRequest.builder()
                .name("SUPERVISOR")
                .description("Supervisor role")
                .build();

        when(roleService.create(any(RoleCreateRequest.class))).thenReturn(sampleRoleResponse);

        mockMvc.perform(post("/cosmetic/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("ADMIN"));
    }

    @Test
    void testGetRoleById_Success() throws Exception {
        when(roleService.getById(1L)).thenReturn(sampleRoleResponse);

        mockMvc.perform(get("/cosmetic/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetRoleById_NotFound() throws Exception {
        when(roleService.getById(99L)).thenThrow(new ResourceNotFoundException("Role not found"));

        mockMvc.perform(get("/cosmetic/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetAllRoles_Paginated() throws Exception {
        PageResponse<RoleResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(sampleRoleResponse), PageRequest.of(0, 10), 1)
        );
        when(roleService.getAll(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/roles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("ADMIN"));
    }

    @Test
    void testUpdateRole_Success() throws Exception {
        RoleUpdateRequest request = RoleUpdateRequest.builder()
                .name("ADMIN_UPDATE")
                .description("Updated")
                .build();

        when(roleService.update(eq(1L), any(RoleUpdateRequest.class))).thenReturn(sampleRoleResponse);

        mockMvc.perform(put("/cosmetic/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteRole_Success() throws Exception {
        mockMvc.perform(delete("/cosmetic/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
