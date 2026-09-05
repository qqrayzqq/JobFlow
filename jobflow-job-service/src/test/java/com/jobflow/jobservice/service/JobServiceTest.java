package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Company;
import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.enums.JobStatus;
import com.jobflow.jobservice.dto.job.CreateJobDto;
import com.jobflow.jobservice.dto.job.UpdateJobDto;
import com.jobflow.jobservice.elasticsearch.JobDocument;
import com.jobflow.jobservice.elasticsearch.JobSearchRepository;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.CompanyRepository;
import com.jobflow.jobservice.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobSearchRepository jobSearchRepository;
    @Mock
    private ElasticsearchOperations operations;
    @Mock
    private CompanyRepository companyRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, jobSearchRepository, operations, companyRepository);
    }

    @Test
    void createJob_companyNotFound_throwsResourceNotFoundException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        CreateJobDto dto = new CreateJobDto("Backend Dev", "desc", 1000, 2000, "Praha", "Java,Spring", JobStatus.PUBLISHED, 1L);

        assertThatThrownBy(() -> jobService.createJob(dto, 5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createJob_notOwner_throwsAccessDeniedException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(new Company("DHL", "Praha", "", 10L)));

        CreateJobDto dto = new CreateJobDto("Backend Dev", "desc", 1000, 2000, "Praha", "Java,Spring", JobStatus.PUBLISHED, 1L);

        assertThatThrownBy(() -> jobService.createJob(dto, 5L)).isInstanceOf(AccessDeniedException.class);
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void createJob_success_savesAndIndexes() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(new Company("DHL", "Praha", "", 5L)));

        Job saved = new Job();
        saved.setStatus(JobStatus.PUBLISHED);
        when(jobRepository.save(any(Job.class))).thenReturn(saved);

        CreateJobDto dto = new CreateJobDto("Backend Dev", "desc", 1000, 2000, "Praha", "Java,Spring", JobStatus.PUBLISHED, 1L);

        Job result = jobService.createJob(dto, 5L);

        assertThat(result).isNotNull();
        verify(jobRepository).save(any(Job.class));
        verify(jobSearchRepository).save(any(JobDocument.class));
    }

    @Test
    void updateJob_notFound_throwsResourceNotFoundException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateJobDto dto = new UpdateJobDto("Backend Dev", "Praha", "desc", JobStatus.PUBLISHED, 1000, 2000, "Java,Spring");

        assertThatThrownBy(() -> jobService.updateJob(1L, dto, 1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateJob_success_updatesAndReindexesDocument() {
        Job existing = new Job();
        existing.setStatus(JobStatus.PUBLISHED);
        existing.setCompanyId(10L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jobRepository.update(any(Job.class))).thenReturn(existing);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(new Company("DHL", "Praha", "", 5L)));

        UpdateJobDto dto = new UpdateJobDto("Backend Dev", "Praha", "desc", JobStatus.PUBLISHED, 1000, 2000, "Java,Spring");

        Job result = jobService.updateJob(1L, dto, 5L);

        assertThat(result.getTitle()).isEqualTo("Backend Dev");
        verify(jobRepository).update(any(Job.class));
        verify(jobSearchRepository).save(any(JobDocument.class));
    }

    @Test
    void getJobById_notFound_throwsResourceNotFoundException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllSkills_returnsDistinctSkills() {
        when(jobRepository.findAllSkills()).thenReturn(Set.of("Java", "Spring"));

        Set<String> result = jobService.getAllSkills();

        assertThat(result).containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    void reindex_mapsAllJobsAndSavesToEs() {
        Job job = new Job();
        job.setStatus(JobStatus.PUBLISHED);
        when(jobRepository.findAll()).thenReturn(List.of(job));

        jobService.reindex();

        verify(jobSearchRepository).saveAll(anyList());
    }
}
