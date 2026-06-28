package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.dto.company.CreateCompanyDto;
import com.jobflow.jobservice.dto.company.UpdateCompanyDto;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Transactional
    public Company createCompany(CreateCompanyDto dto) {
        if (companyRepository.findByName(dto.name()).isPresent()) throw new DuplicateResourceException("Company with this name already exists");
        return companyRepository.save(new Company(dto.name(), dto.city(), dto.description(), dto.userId()));
    }

    @Transactional
    @CacheEvict(value = "companies", key = "#id")
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

    @Transactional
    @CacheEvict(value = "companies", key = "#id")
    public void deleteCompany(Long id) {
        companyRepository.delete(id);
    }

    @Cacheable(value = "companies", key = "#id")
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    public List<Company> getCompaniesByCity(List<String> cities) {
        return companyRepository.findByCities(cities);
    }
}
