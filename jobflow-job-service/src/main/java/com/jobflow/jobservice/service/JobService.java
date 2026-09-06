package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.elasticsearch.JobDocument;
import com.jobflow.jobservice.elasticsearch.JobSearchRepository;
import com.jobflow.jobservice.exception.InvalidStatusTransitionException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.CompanyRepository;
import com.jobflow.jobservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final JobSearchRepository jobSearchRepository;
    private final ElasticsearchOperations operations;
    private final CompanyRepository companyRepository;

    private JobDocument toDocument(Job job) {
        JobDocument doc = new JobDocument();
        doc.setId(job.getId());
        doc.setTitle(job.getTitle());
        doc.setDescription(job.getDescription());
        doc.setSkills(job.getSkills());
        doc.setCity(job.getCity());
        doc.setStatus(job.getStatus().name());
        doc.setSalaryMax(job.getSalaryMax());
        doc.setSalaryMin(job.getSalaryMin());
        return doc;
    }

    @Transactional
    @CacheEvict(value = "skills", allEntries = true)
    public Job createJob(CreateJobDto dto, Long userId) {
        Company company = companyRepository.findById(dto.companyId()).orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if(!company.getUserId().equals(userId)) throw new AccessDeniedException("You can't create that job");
        Job job = jobRepository.save(new Job(dto.title(), dto.city(), dto.description(), dto.companyId(), dto.salaryMax(), dto.salaryMin(), dto.skills(), dto.status()));
        try {
            jobSearchRepository.save(toDocument(job));
        } catch (Exception e) {
            log.error("Failed to index job {} in ES", job.getId(), e);
        }
        return job;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jobs", key = "#id"),
            @CacheEvict(value = "skills", allEntries = true)
    })
    public Job updateJob(Long id, UpdateJobDto dto, Long userId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        Company company = companyRepository.findById(job.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if(!company.getUserId().equals(userId)) throw new AccessDeniedException("You don't own that job");
        JobStatus currStatus = job.getStatus();
        if(!currStatus.equals(dto.status()) && !currStatus.canTransitionTo(dto.status()))
            throw new InvalidStatusTransitionException("Cannot change job status from " + currStatus + " to " + dto.status());
        job.setCity(dto.city());
        job.setDescription(dto.description());
        job.setSkills(dto.skills());
        job.setSalaryMax(dto.salaryMax());
        job.setSalaryMin(dto.salaryMin());
        job.setTitle(dto.title());
        job.setStatus(dto.status());
        job = jobRepository.update(job);
        try {
            jobSearchRepository.save(toDocument(job));
        } catch (Exception e) {
            log.error("Failed to index job {} in ES", job.getId(), e);
        }
        return job;
    }

    @Transactional
    @Caching(evict =  {
        @CacheEvict(value = "jobs", key = "#id"),
        @CacheEvict(value = "skills", allEntries = true)
    })
    public void deleteJob(Long id, Long userId, String role) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if(!Objects.equals(role, "ADMIN")){
            Company company = companyRepository.findById(job.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            if(!company.getUserId().equals(userId)) throw new AccessDeniedException("You don't own that job");
        }
        jobRepository.delete(id);
        try {
            jobSearchRepository.deleteById((id));
        } catch (Exception e) {
            log.error("Failed to index job {} in ES", job.getId(), e);
        }
    }

    @Cacheable(value = "jobs", key = "#id", unless = "#result.status.name() != 'PUBLISHED'")
    public Job getJobById(Long id, Long userId) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if(job.getStatus().equals(JobStatus.DRAFT)){
            Company company = companyRepository.findById(job.getCompanyId()).orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            if(userId == null || !company.getUserId().equals(userId)) throw new AccessDeniedException("You don't have access to this job");
        }
        return job;
    }

    public List<Job> getJobsByCompany(Long companyId, Long userId) {
        boolean isOwner = userId != null && companyRepository.findById(companyId)
                .map(company -> company.getUserId().equals(userId))
                .orElse(false);
        return isOwner ? jobRepository.findAllByCompanyId(companyId) : jobRepository.findByCompanyId(companyId);
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        if(status == JobStatus.DRAFT) throw new AccessDeniedException("Can't list draft jobs");
        return jobRepository.findByStatus(status);
    }

    public List<Job> getJobsBySalaryRange(Integer min, Integer max) {
        return jobRepository.findBySalaryRange(min, max);
    }

    public List<Job> getJobsByCity(String city) {
        return jobRepository.findByCity(city);
    }

    @Cacheable("skills")
    public Set<String> getAllSkills() {
        return jobRepository.findAllSkills();
    }

    public List<JobDocument> searchJobs(String text, String city, String status, Integer minSalary, Integer maxSalary){
        if (status != null && status.equalsIgnoreCase(JobStatus.DRAFT.name())) {
            throw new AccessDeniedException("Can't search draft jobs");
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q->q.bool(b->{
                    if(text != null && !text.isBlank()){
                        b.must(m -> m.multiMatch(mm -> mm.query(text)
                                .fields("title", "description", "skills")));
                    }else{
                        b.must(m -> m.matchAll(ma -> ma));
                    }
                    if (city != null && !city.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("city").value(city)));
                    }
                    if (status != null && !status.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("status").value(status)));
                    }
                    if (minSalary != null) {
                        b.filter(f -> f.range(r -> r.number(n -> n.field("salaryMin").gte(minSalary.doubleValue()))));
                    }
                    if (maxSalary != null) {
                        b.filter(f -> f.range(r -> r.number(n -> n.field("salaryMax").lte(maxSalary.doubleValue()))));
                    }
                    b.mustNot(mn -> mn.term(t -> t.field("status").value(JobStatus.DRAFT.name())));
                    return b;
                }))
                .build();
        return operations.search(query, JobDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .toList();
    }

    // NOTE: loads the whole table at once; for large datasets use keyset pagination
    // (WHERE id > lastId ORDER BY id LIMIT n) + bulk saveAll per batch.
    public void reindex() {
        List<Job> allJobs = jobRepository.findAll();
        List<JobDocument> jobDocuments = allJobs.stream().map(this::toDocument).toList();
        jobSearchRepository.saveAll(jobDocuments);
    }
}
