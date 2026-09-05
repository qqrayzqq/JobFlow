package com.jobflow.jobservice.service;

import com.jobflow.jobservice.domain.Subscription;
import com.jobflow.jobservice.domain.User;
import com.jobflow.jobservice.dto.subscription.CreateSubscriptionDto;
import com.jobflow.jobservice.exception.DuplicateResourceException;
import com.jobflow.jobservice.exception.ResourceNotFoundException;
import com.jobflow.jobservice.repository.SubscriptionRepository;
import com.jobflow.jobservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp(){
        subscriptionService = new SubscriptionService(subscriptionRepository, userRepository);
    }

    @Test
    void createSubscription_userNotFound_throwsResourceNotFoundException() {
        Long userId = 1L;
        CreateSubscriptionDto dto = new CreateSubscriptionDto("Java");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto, userId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSubscription_duplicateSkill_throwsDuplicateResourceException() {
        Long userId = 1L;
        CreateSubscriptionDto dto = new CreateSubscriptionDto("Java");

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(subscriptionRepository.findByUserIdAndSkill(userId, dto.skill())).thenReturn(Optional.of(new Subscription()));

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto, userId)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createSubscription_success_savesSubscription() {
        Long userId = 1L;
        CreateSubscriptionDto dto = new CreateSubscriptionDto("Java");

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(subscriptionRepository.findByUserIdAndSkill(userId, dto.skill())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(new Subscription());

        Subscription result = subscriptionService.createSubscription(dto, userId);

        assertThat(result).isNotNull();
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void deleteSubscription_notFound_throwsResourceNotFoundException() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deleteSubscription(1L, 5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteSubscription_notOwner_throwsAccessDeniedException() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(new Subscription(10L, "Java")));

        assertThatThrownBy(() -> subscriptionService.deleteSubscription(1L, 5L)).isInstanceOf(AccessDeniedException.class);
        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void deleteSubscription_success_deletesSubscription() {
        Long userId = 5L;
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(new Subscription(userId, "Java")));

        subscriptionService.deleteSubscription(1L, userId);

        verify(subscriptionRepository).delete(1L);
    }

    @Test
    void getSubscriptionsByUser_notOwnUser_throwsAccessDeniedException() {
        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByUser(1L, 5L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getSubscriptionsByUser_success_returnsSubscriptions() {
        Long userId = 5L;
        when(subscriptionRepository.findByUserId(userId)).thenReturn(List.of(new Subscription(userId, "Java")));

        List<Subscription> result = subscriptionService.getSubscriptionsByUser(userId, userId);

        assertThat(result).hasSize(1);
    }
}
