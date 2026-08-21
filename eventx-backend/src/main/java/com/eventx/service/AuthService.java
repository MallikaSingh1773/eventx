package com.eventx.service;

import com.eventx.dto.request.LoginRequest;
import com.eventx.dto.request.RefreshTokenRequest;
import com.eventx.dto.request.RegisterRequest;
import com.eventx.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(Long userId);
}
