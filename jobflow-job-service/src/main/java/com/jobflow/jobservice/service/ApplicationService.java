package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.dto.application.CreateApplicationDto;
import com.jobflow.jobservice.dto.application.UpdateApplicationStatusDto;
import com.jobflow.jobservice.repository.ApplicationRepository;
import com.jobflow.jobservice.repository.JobRepository;
import com.jobflow.jobservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public Application createApplication(CreateApplicationDto dto) {
        if (jobRepository.findById(dto.jobId()).isEmpty()) throw new IllegalArgumentException("job with this id doesn't exist");
        if (userRepository.findById(dto.candidateId()).isEmpty()) throw new IllegalArgumentException("candidate with this id doesn't exist");
        if (applicationRepository.findByJobIdAndCandidateId(dto.jobId(), dto.candidateId()).isPresent()) throw new IllegalArgumentException("application already exists");
        return applicationRepository.save(new Application(dto.jobId(), dto.candidateId()));
    }

    public Application updateStatus(Long id, UpdateApplicationStatusDto dto) {
        if (applicationRepository.findById(id).isEmpty()) throw new IllegalArgumentException("application with this id doesn't exist");
        return applicationRepository.updateStatus(id, dto.status());
    }

    public void deleteApplication(Long id) {
        applicationRepository.delete(id);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("application with this id doesn't exist"));
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }
}
