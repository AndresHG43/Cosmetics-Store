package com.store.cosmetic.service.implementation;

import com.store.cosmetic.domain.dto.request.AuthLoginRequest;
import com.store.cosmetic.domain.dto.request.PasswordGenerateRequest;
import com.store.cosmetic.domain.dto.request.RefreshTokenRequest;
import com.store.cosmetic.domain.dto.response.AuthResponse;
import com.store.cosmetic.domain.dto.response.PasswordGenerateResponse;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import com.store.cosmetic.exception.UnauthorizedException;
import com.store.cosmetic.repository.UserRepository;
import com.store.cosmetic.repository.UserRoleRepository;
import com.store.cosmetic.service.contract.AuthService;
import com.store.cosmetic.service.contract.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public AuthServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActiveTrue(user.getId());
        List<String> roleNames = userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        String token = jwtService.generateToken(user.getEmail(), user.getId(), roleNames);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName() + " " + user.getLastname())
                .roles(roleNames)
                .build();
    }

    @Override
    public PasswordGenerateResponse passwordGenerate(PasswordGenerateRequest request) {
        String encoded = passwordEncoder.encode(request.getPassword());
        return PasswordGenerateResponse.builder()
                .encodedPassword(encoded)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        if (username == null) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User account is inactive or not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActiveTrue(user.getId());
        List<String> roleNames = userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getId(), roleNames);
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName() + " " + user.getLastname())
                .roles(roleNames)
                .build();
    }
}
