package com.eventx.service.impl;

import com.eventx.dto.request.LoginRequest;
import com.eventx.dto.request.RefreshTokenRequest;
import com.eventx.dto.request.RegisterRequest;
import com.eventx.dto.response.AuthResponse;
import com.eventx.entity.RefreshToken;
import com.eventx.entity.User;
import com.eventx.entity.enums.Role;
import com.eventx.exception.DuplicateResourceException;
import com.eventx.repository.RefreshTokenRepository;
import com.eventx.repository.UserRepository;
import com.eventx.security.JwtTokenProvider;
import com.eventx.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(existing -> signInExistingUser(existing, request.getPassword()))
                .orElseGet(() -> createUser(request));
    }

    private AuthResponse signInExistingUser(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new DuplicateResourceException("This email already has an account. Sign in with your password.");
        }
        return issueTokens(user);
    }

    private AuthResponse createUser(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(resolveRegisterRole(request.getAccountType()));
        user.setActive(true);
        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return issueTokens(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(RefreshToken::new);
        refreshToken.setUser(user);
        refreshToken.setToken(jwtTokenProvider.generateRefreshToken());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }

    private Role resolveRegisterRole(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return Role.ROLE_USER;
        }
        String normalized = accountType.trim().toUpperCase().replace("ROLE_", "");
        if ("ORGANIZER".equals(normalized)) {
            return Role.ROLE_ORGANIZER;
        }
        return Role.ROLE_USER;
    }
}
