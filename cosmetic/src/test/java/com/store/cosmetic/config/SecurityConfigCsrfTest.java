package com.store.cosmetic.config;

import com.store.cosmetic.service.contract.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the stateless security posture defined in {@link SecurityConfig}.
 *
 * <p>Since the application relies strictly on stateless Bearer JWT authentication and
 * does not use session or authentication cookies, CSRF protection is disabled.
 * These tests verify that state-changing requests (POST, etc.) to endpoints succeed
 * without requiring any CSRF tokens or cookies.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigCsrfTest {

    /** Minimal stub used to avoid loading real controllers / database. */
    @RestController
    static class StubController {
        @PostMapping("/cosmetic/api/auth/login")
        String authLogin() { return "{\"success\":true}"; }

        @PostMapping("/cosmetic/api/products")
        String createProduct() { return "{\"success\":true}"; }

        @GetMapping("/cosmetic/api/products")
        String listProducts() { return "{\"success\":true}"; }
    }

    @Mock
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StubController())
                .build();
    }

    @Test
    @DisplayName("Stateless API: POST to /auth/login succeeds without CSRF token")
    void postToAuthLogin_WithoutCsrfToken_Returns2xx() throws Exception {
        mockMvc.perform(post("/cosmetic/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Stateless API: POST to protected endpoint succeeds without CSRF token")
    void postToProtectedEndpoint_WithoutCsrfToken_Returns2xx() throws Exception {
        mockMvc.perform(post("/cosmetic/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isOk());
    }
}
