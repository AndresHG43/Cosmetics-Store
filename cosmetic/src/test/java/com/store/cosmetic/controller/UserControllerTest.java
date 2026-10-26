package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.UserCreateRequest;
import com.store.cosmetic.domain.dto.request.UserUpdateRequest;
import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.domain.dto.response.UserResponse;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.service.contract.UserService;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = UserResponse.builder()
                .id(1L)
                .name("Ana")
                .lastname("Lopez")
                .email("ana@example.com")
                .active(true)
                .roles(List.of(RoleResponse.builder().id(1L).name("ADMIN").build()))
                .build();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Ana")
                .lastname("Lopez")
                .email("ana@example.com")
                .password("secure123")
                .build();

        when(userService.create(any(UserCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/cosmetic/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ana"))
                .andExpect(jsonPath("$.data.email").value("ana@example.com"));
    }

    @Test
    void testCreateUser_ValidationError() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("") // blank name
                .email("not-an-email")
                .password("123") // too short
                .build();

        mockMvc.perform(post("/cosmetic/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCreateUser_ConflictEmail() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Ana")
                .lastname("Lopez")
                .email("ana@example.com")
                .password("secure123")
                .build();

        when(userService.create(any(UserCreateRequest.class)))
                .thenThrow(new ConflictException("Email already exists"));

        mockMvc.perform(post("/cosmetic/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        when(userService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/cosmetic/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/cosmetic/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetAllUsers_Paginated() throws Exception {
        PageResponse<UserResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1)
        );
        when(userService.getAll(eq("ana"), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/users")
                        .param("search", "ana")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Ana"));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("Ana Maria")
                .lastname("Lopez")
                .email("anamaria@example.com")
                .build();

        when(userService.update(eq(1L), any(UserUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/cosmetic/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/cosmetic/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
