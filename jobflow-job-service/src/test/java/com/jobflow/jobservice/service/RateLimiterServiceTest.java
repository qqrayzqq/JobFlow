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
    void tryAcquire_firstRequest_setsTtlAndReturnsTrue() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("login:vlad")).thenReturn(1L);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isTrue();
        verify(template).expire("login:vlad", Duration.ofMinutes(15));
    }

    @Test
    void tryAcquire_overLimit_returnsFalse() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("login:vlad")).thenReturn(6L);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isFalse();
        verify(template, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void tryAcquire_redisDown_returnsTrue() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("login:vlad")).thenReturn(null);

        boolean allowed = rateLimiterService.tryAcquire("login:vlad", 5, Duration.ofMinutes(15));

        assertThat(allowed).isTrue();
        verify(template, never()).expire(anyString(), any(Duration.class));
    }
}
