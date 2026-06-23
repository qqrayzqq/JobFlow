package com.jobflow.jobservice.dto.job;

import com.jobflow.jobservice.domain.enums.JobStatus;

public record CreateJobDto(
        String title,
        String description,
        Integer salaryMin,
        Integer salaryMax,
        String city,
        String skills,
        JobStatus status,
        Long companyId
) {
}
