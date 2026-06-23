package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.dto.company.CreateCompanyDto;
import com.jobflow.jobservice.dto.company.UpdateCompanyDto;
import com.jobflow.jobservice.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management")
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    @Operation(summary = "Create company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Company> createCompany(@Valid @RequestBody CreateCompanyDto dto) {
        return ResponseEntity.ok(companyService.createCompany(dto));
    }

    @Operation(summary = "Update company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @Valid @RequestBody UpdateCompanyDto dto) {
        return ResponseEntity.ok(companyService.updateCompany(id, dto));
    }

    @Operation(summary = "Delete company")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Company deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get company by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company found"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @Operation(summary = "Get companies by cities")
    @ApiResponse(responseCode = "200", description = "List of companies")
    @GetMapping
    public ResponseEntity<List<Company>> getCompaniesByCity(@RequestParam List<String> cities) {
        return ResponseEntity.ok(companyService.getCompaniesByCity(cities));
    }
}
