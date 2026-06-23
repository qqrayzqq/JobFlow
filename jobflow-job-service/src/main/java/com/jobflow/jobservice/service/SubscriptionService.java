package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Subscription;
import com.jobflow.jobservice.dto.subscription.CreateSubscriptionDto;
import com.jobflow.jobservice.repository.SubscriptionRepository;
import com.jobflow.jobservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public Subscription createSubscription(CreateSubscriptionDto dto) {
        if (userRepository.findById(dto.userId()).isEmpty()) throw new IllegalArgumentException("user with this id doesn't exist");
        if (subscriptionRepository.findByUserIdAndSkill(dto.userId(), dto.skill()).isPresent()) throw new IllegalArgumentException("subscription for this skill already exists");
        return subscriptionRepository.save(new Subscription(dto.userId(), dto.skill()));
    }

    public void deleteSubscription(Long id) {
        subscriptionRepository.delete(id);
    }

    public List<Subscription> getSubscriptionsByUser(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}
