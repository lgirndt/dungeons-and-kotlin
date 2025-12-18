package io.dungeons.integration

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * Test configuration for integration tests.
 *
 * This configuration explicitly scans only the API package to avoid bean conflicts
 * from other modules that might have test-specific implementations.
 *
 * Uses RestTestClient (Spring Boot 4.0+) instead of deprecated TestRestTemplate.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "io.dungeons.api", // API REST controllers and configuration
        "io.dungeons.domain", // Domain services and use cases
        "io.dungeons.persistence", // Persistence layer
        "io.dungeons.integration", // Integration test utilities
    ],
)
class IntegrationTestConfiguration
