package org.java.assesment.quiz_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java.assesment.quiz_service.model.AppUser;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.model.Exam;
import org.java.assesment.quiz_service.model.enums.ExamStatus;
import org.java.assesment.quiz_service.model.enums.UserRole;
import org.java.assesment.quiz_service.repository.AppUserRepository;
import org.java.assesment.quiz_service.repository.CategoryRepository;
import org.java.assesment.quiz_service.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds initial data for H2 in-memory environments (dev + prod on Cloud Run).
 * Both profiles use H2, so seeding runs on every startup.
 *
 * Dev admin:  admin@dev.local / admin123
 * Prod admin: jr.pathfinder@gmail.com / 160219
 */
@Slf4j
@Component
@Profile({"dev", "prod"})   // runs for both — H2 is in-memory so seeding is needed on every start
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ExamRepository     examRepository;
    private final AppUserRepository  userRepository;
    private final PasswordEncoder    passwordEncoder;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    public void run(String... args) {
        seedCategories();
        seedAdminUser();
    }

    // ── Categories + sample exams ─────────────────────────────────────────────

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        Category java   = save(category("Java",        "java",        "Core Java, JVM, Collections, Concurrency, JDK features"));
        Category spring = save(category("Spring Boot", "spring-boot", "Spring Boot, REST, Security, Data JPA, Cloud"));
        Category cpp    = save(category("C++",         "cpp",         "C++ language fundamentals, STL, memory management"));
        Category ai     = save(category("AI",          "ai",          "Machine Learning, Neural Networks, LLMs, Prompt Engineering"));

        // Sample exams so "Generate with AI" and "Import JSON" can be tested immediately
        saveExam("Java Core – Junior",   "Fundamentals: types, collections, exceptions, I/O",    java,   RELEASED);
        saveExam("Java Core – Senior",   "Advanced: concurrency, JVM tuning, streams, modules",  java,   DRAFT);
        saveExam("Spring Boot Basics",   "Auto-configuration, starters, REST, Spring Data JPA",  spring, RELEASED);
        saveExam("Spring Security",      "Authentication, JWT, OAuth2, method-level security",   spring, DRAFT);
        saveExam("Modern C++ (C++17/20)","Move semantics, RAII, smart pointers, STL algorithms", cpp,    DRAFT);
        saveExam("LLMs & Prompt Eng.",   "Transformers, RAG, prompt patterns, evaluation",       ai,     DRAFT);

        log.info("DataSeeder: seeded {} categories and 6 sample exams", categoryRepository.count());
    }

    // ── Admin user ────────────────────────────────────────────────────────────

    private void seedAdminUser() {
        boolean isProd = "prod".equals(activeProfile);
        String email    = isProd ? "jr.pathfinder@gmail.com" : "admin@dev.local";
        String password = isProd ? "160219"                  : "admin123";
        String name     = isProd ? "Admin"                   : "Dev Admin";

        if (userRepository.findByEmail(email).isPresent()) return;

        AppUser admin = AppUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .displayName(name)
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("DataSeeder: admin user created → email={}", email);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final ExamStatus RELEASED = ExamStatus.RELEASED;
    private static final ExamStatus DRAFT    = ExamStatus.DRAFT;

    private Category save(Category c) { return categoryRepository.save(c); }

    private Category category(String name, String slug, String description) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        return c;
    }

    private void saveExam(String name, String description, Category category, ExamStatus status) {
        Exam e = new Exam();
        e.setName(name);
        e.setDescription(description);
        e.setCategory(category);
        e.setStatus(status);
        e.setMaxTimeMinutes(60);
        e.setSuccessPercentage(70);
        examRepository.save(e);
    }
}
