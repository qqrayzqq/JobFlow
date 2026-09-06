package com.jobflow.jobservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate template;
    @Mock
    private ValueOperations<String, String> valueOps;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(template);
    }

    @Test
    void tryAcquire_firstRequestInWindow_returnsTrueWithoutIncrementing() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("login:vlad", "1", Duration.ofMinutes(15))).thenReturn(true);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isTrue();
        verify(valueOps, never()).increment(anyString());
    }

    @Test
    void tryAcquire_withinLimit_returnsTrue() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("login:vlad", "1", Duration.ofMinutes(15))).thenReturn(false);
        when(valueOps.increment("login:vlad")).thenReturn(3L);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isTrue();
    }

    @Test
    void tryAcquire_overLimit_returnsFalse() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("login:vlad", "1", Duration.ofMinutes(15))).thenReturn(false);
        when(valueOps.increment("login:vlad")).thenReturn(6L);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isFalse();
    }

    @Test
    void tryAcquire_incrementReturnsNull_returnsTrue() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("login:vlad", "1", Duration.ofMinutes(15))).thenReturn(false);
        when(valueOps.increment("login:vlad")).thenReturn(null);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isTrue();
    }
}
