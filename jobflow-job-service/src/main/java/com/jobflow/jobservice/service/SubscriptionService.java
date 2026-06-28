package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Subscription;
import com.jobflow.jobservice.dto.subscription.CreateSubscriptionDto;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.SubscriptionRepository;
import com.jobflow.jobservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Subscription createSubscription(CreateSubscriptionDto dto) {
        if (userRepository.findById(dto.userId()).isEmpty()) throw new ResourceNotFoundException("User not found");
        if (subscriptionRepository.findByUserIdAndSkill(dto.userId(), dto.skill()).isPresent())
            throw new DuplicateResourceException("Subscription for this skill already exists");
        return subscriptionRepository.save(new Subscription(dto.userId(), dto.skill()));
    }

    @Transactional
    public void deleteSubscription(Long id) {
        subscriptionRepository.delete(id);
    }

    public List<Subscription> getSubscriptionsByUser(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}
