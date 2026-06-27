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
        Long cnt = template.opsForValue().increment(key);
        if(cnt == null) return true;
        if(cnt == 1) template.expire(key, window);
        return cnt <= limit;
    }
}
