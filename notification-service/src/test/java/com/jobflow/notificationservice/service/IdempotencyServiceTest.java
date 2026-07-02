package com.jobflow.notificationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final Duration TTL = Duration.ofDays(7);

    @Mock
    private StringRedisTemplate template;

    @Mock
    private ValueOperations<String, String> valueOps;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp(){
        idempotencyService = new IdempotencyService(template);
    }

    @Test
    void isFirstProcessing_newId_returnsTrue() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("processed:application:2", "1", TTL)).thenReturn(true);

        boolean result = idempotencyService.isFirstProcessing(2L);

        assertThat(result).isTrue();
    }

    @Test
    void isFirstProcessing_duplicateId_returnsFalse() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent("processed:application:2", "1", TTL)).thenReturn(false);

        boolean result = idempotencyService.isFirstProcessing(2L);

        assertThat(result).isFalse();
    }
}
