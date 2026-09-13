package com.jobflow.jobservice.service;

import com.jobflow.jobservice.config.KafkaTopicConfig;
import com.jobflow.jobservice.event.ApplicationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationIndexListener {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(ApplicationCreatedEvent event){
        kafkaTemplate.send(KafkaTopicConfig.APPLICATION_CREATED_TOPIC, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send ApplicationCreatedEvent for application {}", event.applicationId(), ex);
                    }
                });
    }
}
