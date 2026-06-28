package com.jobflow.jobservice.domain;

import com.jobflow.jobservice.domain.enums.JobStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Job {
    @EqualsAndHashCode.Include
    private Long id;

    private String title;

    private String description;

    private String city;

    private Integer salaryMin;

    private Integer salaryMax;

    private String skills;

    private JobStatus status;

    private LocalDateTime createdAt;

    private Long companyId;

    private Long views;

    public Job(String title, String city, String description, Long companyId, Integer salaryMax, Integer salaryMin, String skills, JobStatus status) {
        this.title = title;
        this.city = city;
        this.description = description;
        this.companyId = companyId;
        this.salaryMax = salaryMax;
        this.salaryMin = salaryMin;
        this.skills = skills;
        this.status = status;
    }
}