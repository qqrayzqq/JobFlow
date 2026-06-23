package com.jobflow.jobservice.dto.job;

import com.jobflow.jobservice.domain.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateJobDto(
        @NotBlank
        @Size(max = 255)
        String title,
        @NotBlank
        @Size(min = 2, max = 255)
        String city,
        @Size(max = 255)
        String description,
        @NotNull
        JobStatus status,
        @NotNull
        @Positive
        Integer salaryMin,
        @NotNull
        @Positive
        Integer salaryMax,
        String skills
) {
}
