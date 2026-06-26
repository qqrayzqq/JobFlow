package com.jobflow.notificationservice.event;

import java.time.Instant;

public record ApplicationCreatedEvent(
        Long applicationId,
        Long jobId,
        Long candidateId,
        String candidateEmail,
        String jobTitle,
        Instant occurredAt
) {
}
