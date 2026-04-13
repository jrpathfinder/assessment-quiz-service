package org.java.assesment.quiz_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestContainersConfig {

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("quiz-service-core")
                .withUsername("ai-trainer-user")
                .withPassword("test");
        postgresContainer.start();
        System.setProperty("DB_URL", postgresContainer.getJdbcUrl());
        System.setProperty("DB_USERNAME", postgresContainer.getUsername());
        System.setProperty("DB_PASSWORD", postgresContainer.getPassword());
        return postgresContainer;
    }
}