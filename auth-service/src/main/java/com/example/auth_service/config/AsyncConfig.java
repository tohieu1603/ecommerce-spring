package com.example.auth_service.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name="eventExecutor")
    public Executor evExecutor() {
        return new TaskExecutorAdapter(
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("auth-event-", 0).factory())
        );
    }
}
