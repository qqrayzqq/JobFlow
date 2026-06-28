package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.elasticsearch.JobDocument;
import com.jobflow.jobservice.elasticsearch.JobSearchRepository;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final JobSearchRepository jobSearchRepository;
    private final ElasticsearchOperations operations;

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
    public Job createJob(CreateJobDto dto) {
        Job job = jobRepository.save(new Job(dto.title(), dto.city(), dto.description(), dto.companyId(), dto.salaryMax(), dto.salaryMin(), dto.skills(), dto.status()));
        jobSearchRepository.save(toDocument(job));
        return job;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jobs", key = "#id"),
            @CacheEvict(value = "skills", allEntries = true)
    })
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
        job = jobRepository.update(job);
        jobSearchRepository.save(toDocument(job));
        return job;
    }

    @Transactional
    @CacheEvict(value = "jobs", key = "#id")
    public void deleteJob(Long id) {
        jobRepository.delete(id);
        jobSearchRepository.deleteById(id);
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

    @Cacheable("skills")
    public Set<String> getAllSkills() {
        return jobRepository.findAllSkills();
    }

    public List<JobDocument> searchJobs(String text, String city, String status, Integer minSalary, Integer maxSalary){
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
