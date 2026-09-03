package com.jobflow.jobservice.service;

import com.jobflow.jobservice.config.KafkaTopicConfig;
import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.application.CreateApplicationDto;
import com.jobflow.jobservice.dto.application.UpdateApplicationStatusDto;
import com.jobflow.jobservice.event.ApplicationCreatedEvent;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.JobNotPublishedException;
import com.jobflow.jobservice.exception.RateLimitExceededException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.ApplicationRepository;
import com.jobflow.jobservice.repository.CompanyRepository;
import com.jobflow.jobservice.repository.JobRepository;
import com.jobflow.jobservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public Application createApplication(CreateApplicationDto dto) {
        String key = "ratelimit:apply:" + dto.candidateId();
        if (!rateLimiterService.tryAcquire(key, 5, Duration.ofMinutes(1))) {
            throw new RateLimitExceededException("Too many applications, try again later");
        }

        Job job = jobRepository.findById(dto.jobId()).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getStatus() != JobStatus.PUBLISHED)
            throw new JobNotPublishedException("Job is not accepting applications");
        User candidate = userRepository.findById(dto.candidateId()).orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        if (applicationRepository.findByJobIdAndCandidateId(dto.jobId(), dto.candidateId()).isPresent())
            throw new DuplicateResourceException("Application already exists");

        Application saved = applicationRepository.save(new Application(dto.jobId(), dto.candidateId()));
        ApplicationCreatedEvent applicationCreatedEvent = new ApplicationCreatedEvent(saved.getId(), saved.getJobId(), saved.getCandidateId(), candidate.getEmail(), job.getTitle(), Instant.now());
        kafkaTemplate.send(KafkaTopicConfig.APPLICATION_CREATED_TOPIC, applicationCreatedEvent);
        return saved;
    }

    @Transactional
    public Application updateStatus(Long id, UpdateApplicationStatusDto dto, Long userId) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        Company company = companyRepository.findById(job.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if(!company.getUserId().equals(userId)) throw new AccessDeniedException("You don't own the job for this application");
        return applicationRepository.updateStatus(id, dto.status());
    }

    @Transactional
    public void deleteApplication(Long id, Long userId, String role) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if(!application.getCandidateId().equals(userId) && !Objects.equals(role, "ADMIN"))
            throw new AccessDeniedException("You don't own that application");
        applicationRepository.delete(id);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }
}
