package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.AuthLoginRequest;
import com.store.cosmetic.domain.dto.request.PasswordGenerateRequest;
import com.store.cosmetic.domain.dto.request.RefreshTokenRequest;
import com.store.cosmetic.domain.dto.response.AuthResponse;
import com.store.cosmetic.domain.dto.response.PasswordGenerateResponse;

public interface AuthService {

    AuthResponse login(AuthLoginRequest request);

    PasswordGenerateResponse passwordGenerate(PasswordGenerateRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
