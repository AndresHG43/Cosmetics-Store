package com.store.cosmetic.service;

import com.store.cosmetic.service.implementation.JwtServiceImpl;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private static final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();

        // Inject key resources and expiration
        setField(jwtService, "privateKeyResource", new ClassPathResource("keys/private_key.pem"));
        setField(jwtService, "publicKeyResource", new ClassPathResource("keys/public_key.pem"));
        setField(jwtService, "jwtExpiration", EXPIRATION);
        setField(jwtService, "refreshExpiration", 86400000L);

        jwtService.init();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testGenerateAndExtractToken() {
        String username = "admin@cosmetic.com";
        Long userId = 42L;
        List<String> roles = List.of("ADMIN", "ROOT");

        String token = jwtService.generateToken(username, userId, roles);
        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(username, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(roles, jwtService.extractRoles(token));

        LocalDateTime expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.isAfter(LocalDateTime.now()));

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testTokenHeaderHasRS256AndTypJWT() {
        String token = jwtService.generateToken("admin@cosmetic.com", 1L, List.of("ADMIN"));
        assertNotNull(token);

        var header = Jwts.parser()
                .verifyWith(jwtService.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getHeader();

        assertEquals("RS256", header.getAlgorithm());
        assertEquals("JWT", header.getType());
    }

    @Test
    void testValidateTokenWithUserDetails() {
        String username = "user@cosmetic.com";
        Long userId = 5L;
        List<String> roles = List.of("USER");

        String token = jwtService.generateToken(username, userId, roles);

        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UserDetails otherUser = User.builder()
                .username("other@cosmetic.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testExpiredToken() {
        jwtService.setJwtExpiration(-1000); // Already expired
        String token = jwtService.generateToken("expired@cosmetic.com", 1L, List.of("USER"));

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        String username = "refresh@cosmetic.com";
        Long userId = 99L;
        jwtService.setRefreshExpiration(604800000L); // 7 days

        String refreshToken = jwtService.generateRefreshToken(username, userId);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());

        assertEquals(username, jwtService.extractUsername(refreshToken));
        assertEquals(userId, jwtService.extractUserId(refreshToken));
        assertEquals("refresh", jwtService.extractTokenType(refreshToken));

        assertTrue(jwtService.isRefreshToken(refreshToken));
        assertFalse(jwtService.isAccessToken(refreshToken));

        var header = Jwts.parser()
                .verifyWith(jwtService.getPublicKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getHeader();
        assertEquals("RS256", header.getAlgorithm());
        assertEquals("JWT", header.getType());
    }

    @Test
    void testAccessTokenClaims() {
        String token = jwtService.generateToken("access@cosmetic.com", 1L, List.of("USER"));
        assertEquals("access", jwtService.extractTokenType(token));
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
    }
}
