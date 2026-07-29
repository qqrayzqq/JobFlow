package com.jobflow.jobservice.dto.user;

import com.jobflow.jobservice.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserAdminResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        LocalDateTime createdAt
) {
}
