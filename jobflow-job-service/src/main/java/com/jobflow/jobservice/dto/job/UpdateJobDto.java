package com.jobflow.jobservice.dto.job;

import com.jobflow.jobservice.domain.enums.JobStatus;

public record UpdateJobDto(
        String title,
        String city,
        String description,
        JobStatus status,
        Integer salaryMin,
        Integer salaryMax,
        String skills
) {
}
