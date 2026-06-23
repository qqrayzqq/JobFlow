package com.jobflow.jobservice.controller;

import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.dto.application.CreateApplicationDto;
import com.jobflow.jobservice.dto.application.UpdateApplicationStatusDto;
import com.jobflow.jobservice.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody CreateApplicationDto dto) {
        return ResponseEntity.ok(applicationService.createApplication(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Application> updateStatus(@PathVariable Long id, @RequestBody UpdateApplicationStatusDto dto) {
        return ResponseEntity.ok(applicationService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getApplicationsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Application>> getApplicationsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidateId));
    }
}
