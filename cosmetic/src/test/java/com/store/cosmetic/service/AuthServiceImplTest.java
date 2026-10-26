package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.AuthLoginRequest;
import com.store.cosmetic.domain.dto.request.PasswordGenerateRequest;
import com.store.cosmetic.domain.dto.request.RefreshTokenRequest;
import com.store.cosmetic.domain.dto.response.AuthResponse;
import com.store.cosmetic.domain.dto.response.PasswordGenerateResponse;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import com.store.cosmetic.exception.UnauthorizedException;
import com.store.cosmetic.repository.UserRepository;
import com.store.cosmetic.repository.UserRoleRepository;
import com.store.cosmetic.service.contract.JwtService;
import com.store.cosmetic.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private Role adminRole;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .name("Admin")
                .lastname("Cosmetic")
                .email("admin@cosmetic.com")
                .password("encoded_secret")
                .active(true)
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name("ADMIN")
                .build();

        userRole = UserRole.builder()
                .id(10L)
                .user(activeUser)
                .role(adminRole)
                .active(true)
                .build();
    }

    @Test
    void testLogin_Success() {
        AuthLoginRequest request = new AuthLoginRequest("admin@cosmetic.com", "secret123");

        when(userRepository.findByEmail("admin@cosmetic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret123", "encoded_secret")).thenReturn(true);
        when(userRoleRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(userRole));
        when(jwtService.generateToken(eq("admin@cosmetic.com"), eq(1L), anyList())).thenReturn("mock.jwt.token");
        when(jwtService.generateRefreshToken("admin@cosmetic.com", 1L)).thenReturn("mock.refresh.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("mock.refresh.token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("admin@cosmetic.com", response.getEmail());
        assertTrue(response.getRoles().contains("ADMIN"));
    }

    @Test
    void testLogin_UserNotFound() {
        AuthLoginRequest request = new AuthLoginRequest("unknown@cosmetic.com", "secret123");

        when(userRepository.findByEmail("unknown@cosmetic.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_InactiveUser() {
        activeUser.setActive(false);
        AuthLoginRequest request = new AuthLoginRequest("admin@cosmetic.com", "secret123");

        when(userRepository.findByEmail("admin@cosmetic.com")).thenReturn(Optional.of(activeUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_WrongPassword() {
        AuthLoginRequest request = new AuthLoginRequest("admin@cosmetic.com", "wrongpassword");

        when(userRepository.findByEmail("admin@cosmetic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_secret")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testPasswordGenerate() {
        PasswordGenerateRequest request = new PasswordGenerateRequest("mypassword");
        when(passwordEncoder.encode("mypassword")).thenReturn("$2a$10$hashedpassword");

        PasswordGenerateResponse response = authService.passwordGenerate(request);

        assertNotNull(response);
        assertEquals("$2a$10$hashedpassword", response.getEncodedPassword());
    }

    @Test
    void testRefreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");

        when(jwtService.isRefreshToken("valid.refresh.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.refresh.token")).thenReturn("admin@cosmetic.com");
        when(userRepository.findByEmail("admin@cosmetic.com")).thenReturn(Optional.of(activeUser));
        when(userRoleRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(userRole));
        when(jwtService.generateToken(eq("admin@cosmetic.com"), eq(1L), anyList())).thenReturn("new.access.token");
        when(jwtService.generateRefreshToken("admin@cosmetic.com", 1L)).thenReturn("new.refresh.token");

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new.access.token", response.getToken());
        assertEquals("new.refresh.token", response.getRefreshToken());
        assertEquals("admin@cosmetic.com", response.getEmail());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid.refresh.token");
        when(jwtService.isRefreshToken("invalid.refresh.token")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }

    @Test
    void testRefreshToken_InactiveUser() {
        activeUser.setActive(false);
        RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");

        when(jwtService.isRefreshToken("valid.refresh.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.refresh.token")).thenReturn("admin@cosmetic.com");
        when(userRepository.findByEmail("admin@cosmetic.com")).thenReturn(Optional.of(activeUser));

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }
}
