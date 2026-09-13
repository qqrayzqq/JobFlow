package com.jobflow.jobservice.dto.company;

import com.jobflow.jobservice.domain.Company;

public record CompanyResponse(
        Long id,
        String name,
        String description,
        String city
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getDescription(), company.getCity());
    }
}
