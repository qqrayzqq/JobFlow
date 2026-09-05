package com.jobflow.jobservice.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionDto(
        @NotBlank
        String skill
) {
}
