package com.eventx.service;

import com.eventx.dto.response.UserResponse;
import com.eventx.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUser(Long id);
    UserResponse updateUserRole(Long id, Role role);
    UserResponse toggleUserActive(Long id);
    UserResponse getUserProfile(Long userId);
}
