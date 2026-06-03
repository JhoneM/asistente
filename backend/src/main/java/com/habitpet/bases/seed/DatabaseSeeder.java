package com.habitpet.bases.seed;

import com.habitpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "docker"})
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        long total = userRepository.count();
        if (total > 0) {
            log.info("Database already has {} user(s). Flyway seed applied. Seeder skipped.", total);
            return;
        }
        log.warn("Empty database. Test data is loaded via Flyway V2__seed_datos_prueba.sql.");
    }
}
