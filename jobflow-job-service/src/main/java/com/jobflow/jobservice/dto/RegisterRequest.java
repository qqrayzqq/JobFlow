package com.jobflow.jobservice.dto;

import com.jobflow.jobservice.domain.enums.UserRole;

public record RegisterRequest(
        String name,
        String email,
        String password,
        UserRole role
) {
}
