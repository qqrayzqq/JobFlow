package com.jobflow.jobservice.dto.application;

public record CreateApplicationDto(
        Long jobId,
        Long candidateId
) {
}
