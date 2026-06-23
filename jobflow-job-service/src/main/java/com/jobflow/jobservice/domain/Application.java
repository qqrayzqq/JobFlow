package com.jobflow.jobservice.domain;

import com.jobflow.jobservice.domain.enums.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Application {
    @EqualsAndHashCode.Include
    private Long id;

    private ApplicationStatus status;

    private LocalDateTime createdAt;

    private Long jobId;

    private Long candidateId;

    public Application(Long jobId, Long candidateId) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.status = ApplicationStatus.PENDING;
    }
}
