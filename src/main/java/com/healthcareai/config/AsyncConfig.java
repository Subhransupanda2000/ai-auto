package com.healthcareai.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Thread pool used for {@code @Async} work: sending notifications
 * (WhatsApp/SMS/Email) and dispatching n8n webhooks without blocking the
 * request thread handling the patient-facing chat/appointment APIs. Also
 * enables {@code @Retryable} for transient external service failures.
 */
@Configuration
@EnableRetry
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("ai-automation-async-");
        executor.initialize();
        return executor;
    }
}
