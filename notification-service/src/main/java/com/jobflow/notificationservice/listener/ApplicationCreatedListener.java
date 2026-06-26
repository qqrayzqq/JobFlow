package com.jobflow.notificationservice.listener;

import com.jobflow.notificationservice.event.ApplicationCreatedEvent;
import com.jobflow.notificationservice.service.EmailService;
import com.jobflow.notificationservice.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationCreatedListener {
    private final EmailService emailService;
    private final IdempotencyService idempotencyService;

    @KafkaListener(topics = "application-created", groupId = "notification-service")
    public void onApplicationCreated(ApplicationCreatedEvent event){
        if (!idempotencyService.isFirstProcessing(event.applicationId())) {
            log.info("Duplicate event for application {}, skipping", event.applicationId());
            return;
        }
        emailService.sendApplicationConfirmation(event.candidateEmail(), event.jobTitle());
    }
}
