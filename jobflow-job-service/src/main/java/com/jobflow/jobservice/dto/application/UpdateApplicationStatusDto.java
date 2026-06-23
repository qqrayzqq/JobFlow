package com.jobflow.jobservice.dto.application;

import com.jobflow.jobservice.domain.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusDto(
        @NotNull
        ApplicationStatus status
) {
}
