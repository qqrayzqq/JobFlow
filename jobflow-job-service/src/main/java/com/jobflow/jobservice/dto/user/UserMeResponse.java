package com.jobflow.jobservice.dto.user;

import com.jobflow.jobservice.domain.enums.UserRole;

public record UserMeResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        Long companyId
) {
}
