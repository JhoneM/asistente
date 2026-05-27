package com.habitpet.shared.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"dev", "docker"})
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    @Override
    @Transactional
    public void run(String... args) {
        log.info("DatabaseSeeder ejecutándose... (implementación pendiente)");
        // TODO: Implementar poblado de base de datos cuando existan las entidades del dominio
        // - seedUsers()
        // - seedHabitsAndPets()
        // - seedCompletionRecords()
    }
}
