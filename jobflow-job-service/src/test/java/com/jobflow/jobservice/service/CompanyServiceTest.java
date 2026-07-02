package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.dto.company.CreateCompanyDto;
import com.jobflow.jobservice.dto.company.UpdateCompanyDto;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    @Mock
    private CompanyRepository companyRepository;

    private CompanyService companyService;

    @BeforeEach
    void setUp(){
        companyService = new CompanyService(companyRepository);
    }

    @Test
    void createCompany_duplicateName_throwsDuplicateResourceException() {
        when(companyRepository.findByName("DHL")).thenReturn(Optional.of(new Company()));

        CreateCompanyDto dto = new CreateCompanyDto(
                "DHL",
                "",
                "Praha",
                2L
        );

        assertThatThrownBy(() -> companyService.createCompany(dto)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createCompany_success_savesCompany() {
        when(companyRepository.findByName("DHL")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(new Company());

        CreateCompanyDto dto = new CreateCompanyDto(
                "DHL",
                "",
                "Praha",
                2L
        );

        Company result = companyService.createCompany(dto);

        assertThat(result).isNotNull();
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void updateCompany_duplicateName_throwsDuplicateResourceException() {
        when(companyRepository.findById(2L)).thenReturn(Optional.of(new Company("OLD", "Praha", "", 2L)));
        when(companyRepository.findByName("DHL")).thenReturn(Optional.of(new Company()));

        UpdateCompanyDto dto = new UpdateCompanyDto(
                "DHL",
                "",
                "Praha"
        );

        assertThatThrownBy(() -> companyService.updateCompany(2L, dto)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateCompany_success_updatesCompany() {
        Company existing = new Company("DHL", "Praha", "", 2L);
        when(companyRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(companyRepository.update(any(Company.class))).thenReturn(existing);

        UpdateCompanyDto dto = new UpdateCompanyDto(
                "DHL",
                "new description",
                "Brno"
        );

        Company result = companyService.updateCompany(2L, dto);

        assertThat(result).isNotNull();
        verify(companyRepository).update(any(Company.class));
        assertThat(result.getCity()).isEqualTo("Brno");
    }

    @Test
    void getCompanyById_notFound_throwsResourceNotFoundException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
