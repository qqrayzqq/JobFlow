package com.jobflow.jobservice.dto.application;

import jakarta.validation.constraints.NotNull;

public record CreateApplicationDto(
        @NotNull
        Long jobId,
        @NotNull
        Long candidateId
) {
}
