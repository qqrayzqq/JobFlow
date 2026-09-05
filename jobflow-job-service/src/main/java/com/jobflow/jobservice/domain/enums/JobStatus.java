package com.jobflow.jobservice.domain.enums;

import java.util.ArrayList;
import java.util.List;

public enum JobStatus {
    DRAFT,
    PUBLISHED,
    CLOSED;

    private List<JobStatus> allowedNext = new ArrayList<>();

    static {
        DRAFT.allowedNext = List.of(PUBLISHED);
        PUBLISHED.allowedNext = List.of(CLOSED);
    }

    public boolean canTransitionTo(JobStatus next){return allowedNext.contains(next);}
}