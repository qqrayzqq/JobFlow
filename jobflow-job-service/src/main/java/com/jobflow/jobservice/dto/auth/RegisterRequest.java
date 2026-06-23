package com.jobflow.jobservice.dto.auth;

import com.jobflow.jobservice.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 2, max = 255)
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 6, max = 255)
        String password,
        @NotNull
        UserRole role
) {
}
