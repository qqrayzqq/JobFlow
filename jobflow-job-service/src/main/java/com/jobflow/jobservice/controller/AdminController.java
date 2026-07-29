package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.dto.user.UserAdminResponse;
import com.jobflow.jobservice.repository.ApplicationRepository;
import com.jobflow.jobservice.repository.CompanyRepository;
import com.jobflow.jobservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Read-only oversight for admins")
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;

    @Operation(summary = "List all users (admin oversight)")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        List<UserAdminResponse> users = userRepository.findAll().stream()
                .map(u -> new UserAdminResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "List all companies (admin oversight)")
    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @Operation(summary = "List all applications (admin oversight)")
    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationRepository.findAll());
    }
}
