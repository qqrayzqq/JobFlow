package com.jobflow.jobservice.dto.application;

import com.jobflow.jobservice.domain.enums.ApplicationStatus;

public record UpdateApplicationStatusDto(
        ApplicationStatus status
) {
}
