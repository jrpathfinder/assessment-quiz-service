# Nomiq Quiz Service

Spring Boot 4 REST API for Java certification exam management. Handles categories, exams, questions, timed exam attempts, scoring, and AI-powered question generation.

---

## What it does

| Feature | Description |
|---|---|
| **Exam simulation** | Timed, shuffled, server-scored attempts with deadline enforcement |
| **Question management** | CRUD with admin role protection |
| **AI question generation** | Batch generates questions via Claude API (admin only) |
| **Authentication** | JWT (email/password) + Google OAuth2 |
| **Swagger UI** | Interactive API docs at `/swagger-ui.html` |
| **Adaptive trainer integration** | Exposes exam attempt history for the AI trainer microservice |

---

## Architecture

```
React Frontend (:3000)
       │  /api/*  (JWT in Authorization header)
       ▼
Spring Boot (:8080)
  Controllers → Services → Repositories → JPA Entities
       │
  PostgreSQL (:5432)   ←  Flyway migrations (V1–V11)
       │
  Claude API (optional)  ←  AI question generation (admin)
```

**Domain model:**
```
Category → Exam → Question → PossibleAnswer
AppUser  → ExamAttempt → AttemptAnswer
```

---

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java JDK | 21+ | [adoptium.net](https://adoptium.net/) or `brew install temurin@21` |
| Maven | 3.9+ | Bundled — use `./mvnw` (no install needed) |
| Docker Desktop | latest | [docker.com](https://www.docker.com/products/docker-desktop/) |
| Git | any | [git-scm.com](https://git-scm.com/) |

Verify Java:
```bash
java -version
# Expected: openjdk version "21.x.x"
```

---

## Step-by-step local setup

### Step 1 — Clone the repository

```bash
git clone https://github.com/jrpathfinder/assessment-quiz-service.git
cd assessment-quiz-service
```

---

### Step 2 — Choose a run mode

The service supports two profiles:

| Profile | Database | Schema | Best for |
|---|---|---|---|
| `dev` | H2 in-memory (PostgreSQL mode) | Auto-created by Hibernate | Quick start, no Docker needed |
| `prod` (default) | PostgreSQL via env vars | Managed by Flyway | Full fidelity, matches production |

**Use `dev` to get running in under a minute. Use `prod` for full feature testing.**

---

### Step 3a — Quick start with `dev` profile (no Docker required)

The `dev` profile uses an embedded H2 database — no external services needed.

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The service starts on `http://localhost:8080`. On startup, `DataSeeder` automatically creates 4 seed categories.

> **H2 console** (inspect the in-memory database):
> ```
> http://localhost:8080/h2-console
> JDBC URL:  jdbc:h2:mem:testdb
> User:      sa
> Password:  (leave blank)
> ```

---

### Step 3b — Full setup with PostgreSQL (`prod` profile)

#### Start PostgreSQL with Docker

```bash
# From the nomiq/ directory (one level up)
docker-compose up postgres -d

# Or run standalone:
docker run -d \
  --name quiz_postgres \
  -e POSTGRES_DB=quiz_db \
  -e POSTGRES_USER=quiz_user \
  -e POSTGRES_PASSWORD=quiz_pass \
  -p 5432:5432 \
  postgres:16-alpine
```

Wait for it to be ready:
```bash
docker exec quiz_postgres pg_isready -U quiz_user -d quiz_db
# Expected: /var/run/postgresql:5432 - accepting connections
```

#### Configure environment variables

Copy the example and fill in your values:
```bash
cp deploy.env.example deploy.env
```

Edit `deploy.env`:
```bash
# Required for Google OAuth2 login
GOOGLE_OAUTH_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_OAUTH_CLIENT_SECRET=GOCSPX-your-secret

# JWT signing key — must be at least 32 characters
JWT_SECRET=change-me-to-a-long-random-string-here

# Optional — only needed for AI question generation
CLAUDE_API_KEY=sk-ant-...

# Database (defaults work with the Docker container above)
DB_USER=quiz_user
DB_PASSWORD=quiz_pass
```

> **Google OAuth2:** If you don't need Google login yet, the service still starts — the OAuth2 endpoints simply won't work. You can register a project at [console.cloud.google.com](https://console.cloud.google.com/) later.

#### Run with PostgreSQL

```bash
source deploy.env   # load env vars into current shell

./mvnw spring-boot:run
# Service starts on http://localhost:8080
# Flyway runs migrations V1–V11 automatically on first start
```

---

### Step 4 — Verify the service is running

```bash
curl http://localhost:8080/api/hello
# Expected: 200 OK

# Or open Swagger UI:
open http://localhost:8080/swagger-ui.html   # macOS
# http://localhost:8080/swagger-ui.html in browser
```

---

### Step 5 — Register a user and get a JWT

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "displayName": "Test User", "password": "secret123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "secret123"}'

# Response: {"token": "eyJ...", "email": "test@example.com", "role": "USER"}
```

Save the token:
```bash
TOKEN="eyJ..."   # paste your token here
```

---

### Step 6 — Try the API

```bash
# List categories
curl http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN"

# List exams in a category
curl http://localhost:8080/api/exams?categoryId=1 \
  -H "Authorization: Bearer $TOKEN"

# Start an exam attempt
curl -X POST http://localhost:8080/api/exams/1/start \
  -H "Authorization: Bearer $TOKEN"

# Check attempt history
curl http://localhost:8080/api/exams/1/attempts \
  -H "Authorization: Bearer $TOKEN"
```

All available endpoints are documented interactively at `http://localhost:8080/swagger-ui.html` — you can authorize with your JWT token there and test every endpoint in the browser.

---

## Running tests

```bash
# All tests (unit + controller + integration)
./mvnw test

# Single test class
./mvnw test -Dtest=QuestionServiceTest

# Single test method
./mvnw test -Dtest=ExamAttemptServiceTest#submitAfterDeadlineShouldFail

# Skip tests (build only)
./mvnw clean package -DskipTests
```

**Test types:**

| Type | Annotation | Database |
|---|---|---|
| Unit | `@ExtendWith(MockitoExtension.class)` | Mocked repositories |
| Controller | `@SpringBootTest` + MockMvc | H2 in-memory (`dev` profile) |
| Integration | `TestContainersConfig` | Real PostgreSQL (Docker required) |

> Integration tests spin up a real PostgreSQL container via Testcontainers. Docker must be running.

---

## Building

```bash
# Build JAR (with tests)
./mvnw clean package

# Build JAR (skip tests — faster)
./mvnw clean package -DskipTests

# Output: target/quiz-service-0.0.1-SNAPSHOT.jar
```

---

## Running with Docker

**Build the image:**
```bash
./mvnw clean package -DskipTests
docker build -t quiz-service .
```

**Run the container:**
```bash
docker run -d \
  --name quiz-service \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/quiz_db \
  -e DB_USER=quiz_user \
  -e DB_PASSWORD=quiz_pass \
  -e JWT_SECRET=change-me-to-a-long-random-string \
  -e FRONTEND_URL=http://localhost:3000 \
  quiz-service
```

> On macOS/Windows use `host.docker.internal` to reach services on the host machine. On Linux use `--network host` or the container's IP.

Or use docker-compose from `nomiq/`:
```bash
# Build first, then compose will use the JAR
./mvnw clean package -DskipTests
docker-compose up quiz-service
```

---

## Admin operations

Admin endpoints (create/update/delete categories, exams, questions) require `ROLE_ADMIN`. There is currently no self-registration path for admin — set the role directly in the database:

```sql
-- Connect to PostgreSQL
UPDATE app_user SET role = 'ADMIN' WHERE email = 'your@email.com';
```

**AI question generation (admin only):**
```bash
curl -X POST http://localhost:8080/api/questions/ai-generate \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "examId": 1,
    "topic": "Java Streams API",
    "javaVersion": "21",
    "count": 5
  }'
```

Requires `CLAUDE_API_KEY` to be set. Generated questions start with `BETA` status and must be manually reviewed before being published (`RELEASED`).

---

## Database migrations

Schema is managed by Flyway. Migration files live in:
```
src/main/resources/db/migration/
```

| Migration | Description |
|---|---|
| V1 | Initial question table |
| V2 | Full quiz schema (category → exam → question → possible_answer) |
| V3 | Add Python category |
| V4 | Seed Java Core exam |
| V5 | Create exam_attempt table |
| V6 | Fix Java Core question references |
| V7 | Upgrade exam_attempt (deadline, scoring, status) |
| V8 | Add explanation column to question |
| V9 | Remove non-Java categories |
| V10 | Create app_user table |
| V11 | Link exam_attempt to app_user |

**Adding a new migration:**
```bash
# Name format: V{next_number}__{description}.sql
touch src/main/resources/db/migration/V12__your_description.sql
```

Flyway runs automatically on startup in `prod` profile. Never edit existing migration files — always add new ones.

---

## Deploying to Google Cloud Run

```bash
# 1. Fill in deploy.env (copy from deploy.env.example)
cp deploy.env.example deploy.env
# Edit deploy.env with your secrets

# 2. Run the deploy script
./deploy.sh
```

The script will:
1. Build the Spring Boot JAR
2. Build and push a Docker image to GCP Artifact Registry
3. Deploy to Cloud Run in `us-central1`

**GCP resources used:**
- Project: `quiz-trainer-491807`
- Service: `quiz-service` (Cloud Run, `us-central1`)
- Container registry: `quiz-repo` (Artifact Registry)
- Memory: 512Mi, CPU: 1, min instances: 0, max: 3

> **Requirements:** `gcloud` CLI installed and authenticated (`gcloud auth login`), and the GCP project must have Cloud Run and Artifact Registry APIs enabled.

---

## Troubleshooting

**`Port 8080 already in use`:**
```bash
lsof -i :8080
kill -9 <PID>
# Or run on a different port:
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev --server.port=8090"
```

**`FlywayException: Found non-empty schema(s)`:**
The database already has tables but no Flyway history. Run with `baseline-on-migrate: true` once, then revert. Or drop and recreate the database:
```bash
docker exec -it quiz_postgres psql -U quiz_user -d postgres \
  -c "DROP DATABASE quiz_db; CREATE DATABASE quiz_db;"
```

**`Connection to localhost:5432 refused`:**
PostgreSQL container is not running:
```bash
docker start quiz_postgres
# or
docker-compose up postgres -d
```

**`JWT signature does not match`:**
Your `JWT_SECRET` changed between sessions. Tokens signed with the old secret are invalid. Log in again to get a new token.

**`401 Unauthorized` on all requests:**
The JWT is expired (default 24h) or malformed. Re-login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "your@email.com", "password": "yourpassword"}'
```

**Google OAuth2 not working (`[missing_key]` errors):**
`GOOGLE_OAUTH_CLIENT_ID` or `GOOGLE_OAUTH_CLIENT_SECRET` is not set. Either set them in the environment or use email/password auth for local development.

**Slow startup (> 30 seconds):**
First run downloads dependencies. Subsequent starts are fast (< 5 seconds).

**H2 console not available:**
Only available in `dev` profile. Make sure you started with `--spring.profiles.active=dev`.

---

## Environment variable reference

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/quiz_db` | PostgreSQL JDBC connection URL |
| `DB_USER` | `quiz_user` | Database username |
| `DB_PASSWORD` | `quiz_pass` | Database password |
| `JWT_SECRET` | `change-me-in-production-...` | HS256 signing key (minimum 32 chars) |
| `JWT_EXPIRATION_HOURS` | `24` | Token validity in hours |
| `FRONTEND_URL` | `http://localhost:3000` | Allowed CORS origin |
| `CLAUDE_API_KEY` | *(empty)* | Anthropic API key for AI question generation |
| `GOOGLE_CLIENT_ID` | *(empty)* | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | *(empty)* | Google OAuth2 client secret |
| `PORT` | `8080` | Server port (Cloud Run sets this automatically) |
| `SPRING_PROFILES_ACTIVE` | *(default/prod)* | `dev` for H2, omit for PostgreSQL |

---

## Project structure

```
quiz-service/
├── pom.xml                          Maven dependencies
├── mvnw / mvnw.cmd                  Maven wrapper (no install needed)
├── Dockerfile                       eclipse-temurin:21-jre-alpine
├── deploy.sh                        GCP Cloud Run deploy script
├── deploy.env.example               Secrets template (copy → deploy.env)
│
└── src/
    ├── main/
    │   ├── java/.../quiz_service/
    │   │   ├── QuizServiceApplication.java   Entry point
    │   │   ├── config/               SecurityConfig, OpenApiConfig, WebConfig, DataSeeder
    │   │   ├── controller/           AuthController, CategoryController, ExamController,
    │   │   │                         QuestionController, ExamAttemptController
    │   │   ├── service/              AuthService, AiQuestionService, ExamService,
    │   │   │                         ExamAttemptService, QuestionService, CategoryService
    │   │   ├── model/                AppUser, Category, Exam, Question, PossibleAnswer,
    │   │   │                         ExamAttempt, AttemptAnswer + enums/
    │   │   ├── repository/           Spring Data JPA interfaces
    │   │   ├── dto/                  Request/response DTOs (no entities exposed)
    │   │   ├── security/             JwtService (Nimbus JOSE), OAuth2SuccessHandler
    │   │   └── exception/            Custom exceptions + ControllerAdvice
    │   └── resources/
    │       ├── application.yaml      Main config
    │       └── db/migration/         Flyway SQL migrations V1–V11
    └── test/
        ├── service/                  Unit tests (Mockito)
        ├── controller/               Controller tests (MockMvc)
        └── TestContainersConfig.java Integration test PostgreSQL setup
```

---

## API quick reference

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register with email + password |
| `POST` | `/api/auth/login` | Public | Login, returns JWT |
| `GET` | `/api/auth/google` | Public | Start Google OAuth2 flow |
| `GET` | `/api/categories` | User | List all categories |
| `POST` | `/api/categories` | Admin | Create category |
| `GET` | `/api/exams` | User | List exams (`?categoryId=`) |
| `POST` | `/api/exams` | Admin | Create exam |
| `GET` | `/api/questions` | User | List questions (`?examId=`) |
| `POST` | `/api/questions/ai-generate` | Admin | Generate questions via Claude API |
| `POST` | `/api/questions/batch` | Admin | Batch import questions from JSON |
| `GET` | `/api/exams/{id}/access` | User | Check attempt access status |
| `POST` | `/api/exams/{id}/start` | User | Start timed attempt |
| `POST` | `/api/attempts/{id}/submit` | User | Submit answers, get score |
| `GET` | `/api/exams/{id}/attempts` | User | View attempt history |
| `GET` | `/swagger-ui.html` | Public | Interactive API docs |
| `GET` | `/actuator/health` | Public | Health check |
