package org.java.assesment.quiz_service.config;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds initial categories when running with the "dev" profile (H2 in-memory).
 * Not active in production (PostgreSQL + Flyway V2 handles seeding there).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        List<Category> categories = List.of(
            category("Java",        "java",        "Core Java, JVM, Collections, Concurrency, JDK features"),
            category("Spring Boot", "spring-boot", "Spring Boot, REST, Security, Data JPA, Cloud"),
            category("C++",         "cpp",         "C++ language fundamentals, STL, memory management"),
            category("AI",          "ai",          "Machine Learning, Neural Networks, LLMs, Prompt Engineering")
        );

        categoryRepository.saveAll(categories);
    }

    private Category category(String name, String slug, String description) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        return c;
    }
}
