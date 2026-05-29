package com.example.auth_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

import com.example.auth_service.application.events.KafkaTopics;

@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(KafkaTopics.AUTH_USER_EVENT)
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic sessionEventsTopic() {
        return TopicBuilder.name(KafkaTopics.AUTH_SESSION_EVENT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
