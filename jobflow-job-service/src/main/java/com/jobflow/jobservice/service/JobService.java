package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public Job createJob(CreateJobDto dto) {
        return jobRepository.save(new Job(dto.title(), dto.city(), dto.description(), dto.companyId(), dto.salaryMax(), dto.salaryMin(), dto.skills(), dto.status()));
    }

    public Job updateJob(Long id, UpdateJobDto dto) {
        var jobOptional = jobRepository.findById(id);
        if(jobOptional.isEmpty()) throw new IllegalArgumentException("job with this id doesn't exist");
        Job job = jobOptional.get();
        job.setCity(dto.city());
        job.setDescription(dto.description());
        job.setSkills(dto.skills());
        job.setSalaryMax(dto.salaryMax());
        job.setSalaryMin(dto.salaryMin());
        job.setTitle(dto.title());
        job.setStatus(dto.status());
        return jobRepository.update(job);
    }

    public void deleteJob(Long id) {
        jobRepository.delete(id);
    }

    public Job getJobById(Long id) {
        var jobOptional = jobRepository.findById(id);
        if(jobOptional.isEmpty()) throw new IllegalArgumentException("job with this id doesn't exist");
        return jobOptional.get();
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
