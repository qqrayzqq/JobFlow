package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Job;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewCounterServiceTest {
    @Mock
    private StringRedisTemplate template;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ValueOperations<String, String> valueOps;

    private ViewCounterService viewCounterService;

    @BeforeEach
    void setUp(){
        viewCounterService = new ViewCounterService(template, jobRepository);
    }

    @Test
    void increment_incrementsViewCounter() {
        when(template.opsForValue()).thenReturn(valueOps);

        viewCounterService.increment(1L);

        verify(valueOps).increment("job:views:1");
    }

    @Test
    void getViews_returnsCurrentCount() {
        Job job = new Job();
        job.setViews(5L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("job:views:1")).thenReturn("10");

        long result = viewCounterService.getViews(1L);

        assertThat(result).isEqualTo(15L);
        verify(valueOps).get("job:views:1");
    }

    @Test
    void getViews_jobNotFound_throwsResourceNotFoundException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> viewCounterService.getViews(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void flush_noKeys_doesNothing() {
        when(template.keys("job:views:*")).thenReturn(null);

        viewCounterService.flush();

        verify(jobRepository, never()).addViews(anyLong(), anyLong());
    }

    @Test
    void flush_movesCountsToPostgres() {
        when(template.keys("job:views:*")).thenReturn(Set.of("job:views:1"));
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.getAndDelete("job:views:1")).thenReturn("10");

        viewCounterService.flush();

        verify(jobRepository).addViews(1L, 10L);
    }
}
