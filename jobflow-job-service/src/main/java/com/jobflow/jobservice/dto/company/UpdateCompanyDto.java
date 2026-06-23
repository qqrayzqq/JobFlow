package com.jobflow.jobservice.dto.company;

public record UpdateCompanyDto(
        String name,
        String description,
        String city
) {
}
