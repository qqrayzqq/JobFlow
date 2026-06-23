package com.jobflow.jobservice.dto.job;

import com.jobflow.jobservice.domain.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateJobDto(
        @NotBlank
        @Size(max = 255)
        String title,
        @Size(max = 255)
        String description,
        @Positive
        @NotNull
        Integer salaryMin,
        @Positive
        @NotNull
        Integer salaryMax,
        @NotBlank
        @Size(min = 2, max = 255)
        String city,
        String skills,
        @NotNull
        JobStatus status,
        @NotNull
        Long companyId
) {
}
