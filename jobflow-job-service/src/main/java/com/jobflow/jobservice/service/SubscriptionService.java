package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Subscription;
import com.jobflow.jobservice.dto.subscription.CreateSubscriptionDto;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.SubscriptionRepository;
import com.jobflow.jobservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Subscription createSubscription(CreateSubscriptionDto dto, Long userId) {
        if (userRepository.findById(userId).isEmpty()) throw new ResourceNotFoundException("User not found");
        if (subscriptionRepository.findByUserIdAndSkill(userId, dto.skill()).isPresent())
            throw new DuplicateResourceException("Subscription for this skill already exists");
        return subscriptionRepository.save(new Subscription(userId, dto.skill()));
    }

    @Transactional
    public void deleteSubscription(Long id, Long userId) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        if(!subscription.getUserId().equals(userId)) throw new AccessDeniedException("You can't delete this subscription");
        subscriptionRepository.delete(id);
    }

    public List<Subscription> getSubscriptionsByUser(Long userId, Long realUserId) {
        if(!userId.equals(realUserId)) throw new AccessDeniedException("You can't get these subscriptions");
        return subscriptionRepository.findByUserId(userId);
    }
}
