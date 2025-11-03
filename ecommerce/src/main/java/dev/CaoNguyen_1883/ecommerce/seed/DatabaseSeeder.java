package dev.CaoNguyen_1883.ecommerce.seed;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final List<Seeder> seeders;
    private final SeedDataConfig seedDataConfig;
    private boolean seeded = false;

    @EventListener
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!seedDataConfig.isEnabled()) {
            log.info("Database seeding is disabled");
            return;
        }

        if (seeded && !seedDataConfig.isForceReseed()) {
            log.debug("Database already seeded, skipping...");
            return;
        }

        log.info("========================================");
        log.info("Starting database seeding...");
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        try {
            // Seeders are automatically ordered by @Order annotation
            for (Seeder seeder : seeders) {
                seeder.seed();
            }

            seeded = true;
            long duration = System.currentTimeMillis() - startTime;

            log.info("========================================");
            log.info("Database seeding completed successfully in {}ms", duration);
            log.info("========================================");
        } catch (Exception e) {
            log.error("========================================");
            log.error("Database seeding failed: {}", e.getMessage(), e);
            log.error("========================================");
            throw new RuntimeException("Database seeding failed", e);
        }
    }
}