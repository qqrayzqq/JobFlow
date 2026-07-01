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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        CreateSubscriptionDto dto = new CreateSubscriptionDto(
          1L,
          "Java"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSubscription_duplicateSkill_throwsDuplicateResourceException() {
        CreateSubscriptionDto dto = new CreateSubscriptionDto(
                1L,
                "Java"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(subscriptionRepository.findByUserIdAndSkill(dto.userId(), dto.skill())).thenReturn(Optional.of(new Subscription()));

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createSubscription_success_savesSubscription() {
        CreateSubscriptionDto dto = new CreateSubscriptionDto(
                1L,
                "Java"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(subscriptionRepository.findByUserIdAndSkill(dto.userId(), dto.skill())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(new Subscription());

        Subscription result = subscriptionService.createSubscription(dto);

        assertThat(result).isNotNull();
        verify(subscriptionRepository).save(any(Subscription.class));
    }
}
