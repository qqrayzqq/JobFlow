package com.jobflow.jobservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String APPLICATION_CREATED_TOPIC = "application-created";

    @Bean
    public NewTopic applicationCreatedTopic() {
        return TopicBuilder.name(APPLICATION_CREATED_TOPIC)
                .partitions(3)
                .replicas(1) // single broker in docker-compose
                .build();
    }
}
