# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package           # Build with tests
./mvnw clean package -DskipTests  # Build without tests

# Run
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Test
./mvnw test                                  # All tests
./mvnw test -Dtest=QuestionServiceTest       # Single test class
./mvnw test -Dtest=QuestionServiceTest#methodName  # Single test method

# Deploy (GCP Cloud Run)
./deploy.sh
```

## Architecture

Spring Boot 4 / Java 21 REST service for quiz/exam management, deployed to Google Cloud Run.

**Domain model (hierarchical):**
```
Category → Exam → Question → PossibleAnswer
```
- `Category`: knowledge domain (Java, Spring Boot, C++, AI)
- `Exam`: structured test under a category; has `ExamStatus` (DRAFT, RELEASED)
- `Question`: belongs to exam; has `QuestionStatus` (BETA, RELEASED) and `AnswerType` (RADIO, CHECKBOX)
- `PossibleAnswer`: answer options for a question with `isCorrect` flag

**Layered architecture:** Controller → Service → Repository → Entity. All services use `@Transactional(readOnly = true)` as default, with write methods overriding to `readOnly = false`.

**Profiles:**
- `dev`: H2 in-memory (PostgreSQL-compatible mode), Flyway disabled, Hibernate `create-drop`, H2 console at `/h2-console`, seeds 4 categories via `DataSeeder`
- default (prod): PostgreSQL via env vars `DB_URL`, `DB_USER`, `DB_PASSWORD`; Flyway manages schema; JPA `ddl-auto: validate`

**Database migrations** live in `src/main/resources/db/migration/`. Current schema is `V2__create_quiz_schema.sql`. Add new migrations as `V3__...sql`, `V4__...sql`, etc.

## Testing

- Unit tests: `@ExtendWith(MockitoExtension.class)` with mocked repositories, `@ActiveProfiles("dev")`
- Controller tests: `@SpringBootTest` + MockMvc
- Integration tests: `TestContainersConfig.java` spins up a real PostgreSQL container

Test classes live under `src/test/.../service/` and `src/test/.../controller/`.