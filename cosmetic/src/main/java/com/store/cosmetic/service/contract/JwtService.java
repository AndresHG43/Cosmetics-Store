package com.store.cosmetic.service.contract;

import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface JwtService {

    String generateToken(String username, Long userId, List<String> roles);

    String generateToken(Map<String, Object> extraClaims, String username);

    String generateRefreshToken(String username, Long userId);

    String extractUsername(String token);

    Long extractUserId(String token);

    List<String> extractRoles(String token);

    String extractTokenType(String token);

    LocalDateTime extractExpiration(String token);

    boolean isTokenValid(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isRefreshToken(String token);

    boolean isAccessToken(String token);

    boolean isTokenExpired(String token);
}
