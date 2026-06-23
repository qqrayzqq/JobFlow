package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.dto.company.CreateCompanyDto;
import com.jobflow.jobservice.dto.company.UpdateCompanyDto;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public Company createCompany(CreateCompanyDto dto) {
        if (companyRepository.findByName(dto.name()).isPresent()) throw new DuplicateResourceException("Company with this name already exists");
        return companyRepository.save(new Company(dto.name(), dto.city(), dto.description(), dto.userId()));
    }

    public Company updateCompany(Long id, UpdateCompanyDto dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if (!company.getName().equals(dto.name()) && companyRepository.findByName(dto.name()).isPresent())
            throw new DuplicateResourceException("Company with this name already exists");
        company.setCity(dto.city());
        company.setName(dto.name());
        company.setDescription(dto.description());
        return companyRepository.update(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.delete(id);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    public List<Company> getCompaniesByCity(List<String> cities) {
        return companyRepository.findByCities(cities);
    }
}
