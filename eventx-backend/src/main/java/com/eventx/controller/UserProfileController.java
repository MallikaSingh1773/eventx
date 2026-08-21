package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.UserResponse;
import com.eventx.security.CustomUserDetailsService.CustomUserDetails;
import com.eventx.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile")
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        UserResponse response = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
