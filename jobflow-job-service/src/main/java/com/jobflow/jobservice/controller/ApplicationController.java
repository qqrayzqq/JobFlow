package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.dto.application.CreateApplicationDto;
import com.jobflow.jobservice.dto.application.UpdateApplicationStatusDto;
import com.jobflow.jobservice.service.ApplicationService;
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
@Tag(name = "Applications", description = "Job application management")
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    @Operation(summary = "Apply for a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application created"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate application"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Job or candidate not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Application> createApplication(@Valid @RequestBody CreateApplicationDto dto) {
        return ResponseEntity.ok(applicationService.createApplication(dto));
    }

    @Operation(summary = "Update application status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Application> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateApplicationStatusDto dto) {
        return ResponseEntity.ok(applicationService.updateStatus(id, dto));
    }

    @Operation(summary = "Delete application")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Application deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get application by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('COMPANY')")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @Operation(summary = "Get applications by job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of applications"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<Application>> getApplicationsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    @Operation(summary = "Get applications by candidate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of applications"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<Application>> getApplicationsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidateId));
    }
}
