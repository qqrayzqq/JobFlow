package com.jobflow.jobservice.service;

import com.jobflow.jobservice.config.KafkaTopicConfig;
import com.jobflow.jobservice.domain.Application;
import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.domain.enums.ApplicationStatus;
import com.jobflow.jobservice.dto.application.CreateApplicationDto;
import com.jobflow.jobservice.dto.application.UpdateApplicationStatusDto;
import com.jobflow.jobservice.event.ApplicationCreatedEvent;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.RateLimitExceededException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.ApplicationRepository;
import com.jobflow.jobservice.repository.JobRepository;
import com.jobflow.jobservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private RateLimiterService rateLimiterService;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp(){
        applicationService = new ApplicationService(applicationRepository, jobRepository, userRepository, kafkaTemplate, rateLimiterService);
    }

    @Test
    void createApplication_rateLimitExceeded_throwsRateLimitExceededException() {
        when(rateLimiterService.tryAcquire("ratelimit:apply:2", 5, Duration.ofMinutes(1))).thenReturn(false);

        CreateApplicationDto dto = new CreateApplicationDto(
                1L,
                2L
        );

        assertThatThrownBy(() -> applicationService.createApplication(dto)).isInstanceOf(RateLimitExceededException.class);
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void createApplication_duplicate_throwsDuplicateResourceException() {
        when(rateLimiterService.tryAcquire("ratelimit:apply:2", 5, Duration.ofMinutes(1))).thenReturn(true);

        CreateApplicationDto dto = new CreateApplicationDto(
                1L,
                2L
        );

        when(jobRepository.findById(dto.jobId())).thenReturn(Optional.of(new Job()));
        when(userRepository.findById(dto.candidateId())).thenReturn(Optional.of(new User()));
        when(applicationRepository.findByJobIdAndCandidateId(dto.jobId(), dto.candidateId())).thenReturn(Optional.of(new Application(1L, 2L)));


        assertThatThrownBy(() -> applicationService.createApplication(dto)).isInstanceOf(DuplicateResourceException.class);
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void createApplication_success_savesAndPublishesEvent() {
        when(rateLimiterService.tryAcquire("ratelimit:apply:2", 5, Duration.ofMinutes(1))).thenReturn(true);

        CreateApplicationDto dto = new CreateApplicationDto(
                1L,
                2L
        );

        when(jobRepository.findById(dto.jobId())).thenReturn(Optional.of(new Job()));
        when(userRepository.findById(dto.candidateId())).thenReturn(Optional.of(new User()));
        when(applicationRepository.findByJobIdAndCandidateId(dto.jobId(), dto.candidateId())).thenReturn(Optional.empty());
        when(applicationRepository.save(any(Application.class))).thenReturn(new Application(1L, 2L));

        Application result = applicationService.createApplication(dto);

        assertThat(result).isNotNull();
        verify(applicationRepository).save(any(Application.class));
        verify(kafkaTemplate).send(eq(KafkaTopicConfig.APPLICATION_CREATED_TOPIC), any(ApplicationCreatedEvent.class));
    }

    @Test
    void updateStatus_notFound_throwsResourceNotFoundException() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateApplicationStatusDto dto = new UpdateApplicationStatusDto(ApplicationStatus.REJECTED);

        assertThatThrownBy(() -> applicationService.updateStatus(1L, dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_success_updatesStatus() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(new Application(1L, 1L)));

        UpdateApplicationStatusDto dto = new UpdateApplicationStatusDto(ApplicationStatus.REJECTED);

        Application updated = new Application(1L, 2L);
        updated.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.updateStatus(1L, dto.status())).thenReturn(updated);

        Application result = applicationService.updateStatus(1L, dto);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void getApplicationById_notFound_throwsResourceNotFoundException() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getApplicationById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
