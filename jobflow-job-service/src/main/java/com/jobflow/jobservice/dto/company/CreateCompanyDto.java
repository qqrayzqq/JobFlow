package com.jobflow.jobservice.dto.company;

public record CreateCompanyDto(
        String name,
        String description,
        String city,
        Long userId
) {
}
