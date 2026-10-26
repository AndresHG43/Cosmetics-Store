package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.AuthLoginRequest;
import com.store.cosmetic.domain.dto.request.PasswordGenerateRequest;
import com.store.cosmetic.domain.dto.request.RefreshTokenRequest;
import com.store.cosmetic.domain.dto.response.AuthResponse;
import com.store.cosmetic.domain.dto.response.PasswordGenerateResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.exception.UnauthorizedException;
import com.store.cosmetic.service.contract.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthLoginRequest request = new AuthLoginRequest("admin@cosmetic.com", "secret123");
        AuthResponse response = AuthResponse.builder()
                .token("jwt.token.value")
                .refreshToken("jwt.refresh.value")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .email("admin@cosmetic.com")
                .name("Admin Cosmetic")
                .roles(List.of("ADMIN"))
                .build();

        when(authService.login(any(AuthLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/cosmetic/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.token.value"))
                .andExpect(jsonPath("$.data.refreshToken").value("jwt.refresh.value"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("admin@cosmetic.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    void testLogin_Unauthorized() throws Exception {
        AuthLoginRequest request = new AuthLoginRequest("admin@cosmetic.com", "wrongpassword");

        when(authService.login(any(AuthLoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/cosmetic/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testLogin_ValidationFailure() throws Exception {
        AuthLoginRequest invalidRequest = new AuthLoginRequest("not-an-email", "");

        mockMvc.perform(post("/cosmetic/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testPasswordGenerate_Success() throws Exception {
        PasswordGenerateRequest request = new PasswordGenerateRequest("password123");
        PasswordGenerateResponse response = new PasswordGenerateResponse("$2a$10$encodedHashString");

        when(authService.passwordGenerate(any(PasswordGenerateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/cosmetic/api/auth/password-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.encodedPassword").value("$2a$10$encodedHashString"));
    }

    @Test
    void testPasswordGenerate_ValidationFailure() throws Exception {
        PasswordGenerateRequest request = new PasswordGenerateRequest("");

        mockMvc.perform(post("/cosmetic/api/auth/password-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");
        AuthResponse response = AuthResponse.builder()
                .token("new.jwt.access")
                .refreshToken("new.jwt.refresh")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .email("admin@cosmetic.com")
                .name("Admin Cosmetic")
                .roles(List.of("ADMIN"))
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/cosmetic/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("new.jwt.access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new.jwt.refresh"));
    }

    @Test
    void testRefreshToken_Unauthorized() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("expired.token");

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid or expired refresh token"));

        mockMvc.perform(post("/cosmetic/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }
}
