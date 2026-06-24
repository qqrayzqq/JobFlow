package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public Job createJob(CreateJobDto dto) {
        return jobRepository.save(new Job(dto.title(), dto.city(), dto.description(), dto.companyId(), dto.salaryMax(), dto.salaryMin(), dto.skills(), dto.status()));
    }

    @CacheEvict(value = "jobs", key = "#id")
    public Job updateJob(Long id, UpdateJobDto dto) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setCity(dto.city());
        job.setDescription(dto.description());
        job.setSkills(dto.skills());
        job.setSalaryMax(dto.salaryMax());
        job.setSalaryMin(dto.salaryMin());
        job.setTitle(dto.title());
        job.setStatus(dto.status());
        return jobRepository.update(job);
    }

    @CacheEvict(value = "jobs", key = "#id")
    public void deleteJob(Long id) {
        jobRepository.delete(id);
    }

    @Cacheable(value = "jobs", key = "#id", unless = "#result.status.name() != 'PUBLISHED'")
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    public List<Job> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompanyId(companyId);
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public List<Job> getJobsBySalaryRange(Integer min, Integer max) {
        return jobRepository.findBySalaryRange(min, max);
    }

    public List<Job> getJobsByCity(String city) {
        return jobRepository.findByCity(city);
    }
}
