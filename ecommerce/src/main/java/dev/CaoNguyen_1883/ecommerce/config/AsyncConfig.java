package dev.CaoNguyen_1883.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async processing
 * Enables @Async annotation and configures thread pool
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Thread pool for view tracking
     * Configured with small pool size to avoid resource exhaustion
     */
    @Bean(name = "viewTrackingExecutor")
    public Executor viewTrackingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: minimum threads to keep alive
        executor.setCorePoolSize(2);

        // Max pool size: maximum threads to create
        executor.setMaxPoolSize(5);

        // Queue capacity: pending tasks before rejection
        executor.setQueueCapacity(100);

        // Thread name prefix for debugging
        executor.setThreadNamePrefix("view-tracker-");

        // What to do when queue is full
        executor.setRejectedExecutionHandler((r, e) -> {
            // Just log warning - view tracking is not critical
            log.warn("View tracking queue full, dropping task");
        });

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("Initialized view tracking executor: corePoolSize=2, maxPoolSize=5, queueCapacity=100");

        return executor;
    }
}
