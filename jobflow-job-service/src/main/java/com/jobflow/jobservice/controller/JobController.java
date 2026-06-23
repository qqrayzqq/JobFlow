package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.service.JobService;
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
@Tag(name = "Jobs", description = "Job posting management")
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    @Operation(summary = "Create job posting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Job> createJob(@Valid @RequestBody CreateJobDto dto) {
        return ResponseEntity.ok(jobService.createJob(dto));
    }

    @Operation(summary = "Update job posting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @Valid @RequestBody UpdateJobDto dto) {
        return ResponseEntity.ok(jobService.updateJob(id, dto));
    }

    @Operation(summary = "Delete job posting")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get job by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job found"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @Operation(summary = "Get jobs by company")
    @ApiResponse(responseCode = "200", description = "List of jobs")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Job>> getJobsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(jobService.getJobsByCompany(companyId));
    }

    @Operation(summary = "Get jobs by status")
    @ApiResponse(responseCode = "200", description = "List of jobs")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(@PathVariable JobStatus status) {
        return ResponseEntity.ok(jobService.getJobsByStatus(status));
    }

    @Operation(summary = "Get jobs by salary range")
    @ApiResponse(responseCode = "200", description = "List of jobs")
    @GetMapping("/salary")
    public ResponseEntity<List<Job>> getJobsBySalaryRange(@RequestParam Integer min, @RequestParam Integer max) {
        return ResponseEntity.ok(jobService.getJobsBySalaryRange(min, max));
    }

    @Operation(summary = "Get jobs by city")
    @ApiResponse(responseCode = "200", description = "List of jobs")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Job>> getJobsByCity(@PathVariable String city) {
        return ResponseEntity.ok(jobService.getJobsByCity(city));
    }
}
