package com.jobflow.jobservice.domain.enums;

import java.util.ArrayList;
import java.util.List;

public enum ApplicationStatus {
    PENDING,
    REVIEWED,
    ACCEPTED,
    REJECTED;

    private List<ApplicationStatus> allowedNext = new ArrayList<>();

    static {
        PENDING.allowedNext = List.of(REVIEWED, REJECTED);
        REVIEWED.allowedNext = List.of(ACCEPTED, REJECTED);
    }

    public boolean canTransitionTo(ApplicationStatus next){return allowedNext.contains(next);}
}
