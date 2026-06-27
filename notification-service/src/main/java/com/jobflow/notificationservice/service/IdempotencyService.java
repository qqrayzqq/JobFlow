package com.jobflow.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    // returns true only the FIRST time this id is seen; false on duplicates
    public boolean isFirstProcessing(Long applicationId) {
        Boolean seen = redisTemplate.opsForValue().setIfAbsent("processed:application:" + applicationId, "1", TTL);
        if(seen != null) return seen;
        return true;
    }

    public void release(Long applicationId){
        redisTemplate.delete("processed:application:" + applicationId);
    }
}
