package com.jobflow.jobservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate template;

    public boolean tryAcquire(String key, int limit, Duration window){
        Boolean createdNow = template.opsForValue().setIfAbsent(key, "1", window);
        if(createdNow) return true;
        else {
            Long cnt = template.opsForValue().increment(key);
            if (cnt == null) return true;
            return cnt <= limit;
        }
    }
}
