package com.jobflow.jobservice.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCompanyDto(
        @NotBlank
        @Size(min = 2, max = 255)
        String name,
        @Size(max = 255)
        String description,
        @NotBlank
        @Size(min= 2, max = 255)
        String city,
        @NotNull
        Long userId
) {
}
