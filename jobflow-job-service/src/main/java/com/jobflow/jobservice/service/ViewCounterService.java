package com.jobflow.jobservice.service;

import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViewCounterService {
    private final StringRedisTemplate template;
    private final JobRepository jobRepository;
    private static final String VIEWS_PREFIX = "job:views:";

    public void increment(Long jobId){
        template.opsForValue().increment(VIEWS_PREFIX + jobId);
    }

    public long getViews(Long jobId){
        long persisted = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found")).getViews();
        String pending = template.opsForValue().get(VIEWS_PREFIX + jobId);
        return persisted + (pending == null ? 0 : Long.parseLong(pending));
    }

    @Scheduled(fixedRate = 60000)
    public void flush(){
        Set<String> keys = template.keys("job:views:*");
        if(keys == null) return;
        for(String key : keys){
            String val = template.opsForValue().getAndDelete(key);
            if(val == null) continue;
            long delta = Long.parseLong(val);
            Long jobId = Long.parseLong(key.substring(VIEWS_PREFIX.length()));
            jobRepository.addViews(jobId, delta);
        }
    }
}
