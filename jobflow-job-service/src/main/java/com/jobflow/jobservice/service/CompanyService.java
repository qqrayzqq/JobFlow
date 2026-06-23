package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.dto.company.CreateCompanyDto;
import com.jobflow.jobservice.dto.company.UpdateCompanyDto;
import com.jobflow.jobservice.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public Company createCompany(CreateCompanyDto dto) {
        if(companyRepository.findByName(dto.name()).isPresent()) throw new IllegalArgumentException("company with this name already exist");
        return companyRepository.save(new Company(dto.name(), dto.city(), dto.description(), dto.userId()));
    }

    public Company updateCompany(Long id, UpdateCompanyDto dto) {
        var company = companyRepository.findById(id);
        if(company.isEmpty()) throw new IllegalArgumentException("company with this id doesn't exist");
        if(!company.get().getName().equals(dto.name()) && companyRepository.findByName(dto.name()).isPresent()) throw new IllegalArgumentException("company with this name already exist");
        Company companyReal = company.get();
        companyReal.setCity(dto.city());
        companyReal.setName(dto.name());
        companyReal.setDescription(dto.description());
        return companyRepository.update(companyReal);
    }

    public void deleteCompany(Long id) {
        companyRepository.delete(id);
    }

    public Company getCompanyById(Long id) {
        var company = companyRepository.findById(id);
        if(company.isEmpty()) throw new IllegalArgumentException("company with this id doesn't exist");
        return company.get();
    }

    public List<Company> getCompaniesByCity(List<String> cities) {
        return companyRepository.findByCities(cities);
    }
}
