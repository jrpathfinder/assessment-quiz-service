-- V2: Replace placeholder question table with full quiz domain schema
--     Category → Exam → Question → PossibleAnswer

-- Drop old scaffold tables (safe – no production data yet)
DROP TABLE IF EXISTS question_tags;
DROP TABLE IF EXISTS question;

-- ---------------------------------------------------------------
-- CATEGORY
-- Top-level knowledge domain: Java, Spring Boot, C++, AI, etc.
-- ---------------------------------------------------------------
CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(100) NOT NULL UNIQUE,   -- url-safe: "spring-boot", "cpp"
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------
-- EXAM
-- A structured test belonging to a category.
-- ---------------------------------------------------------------
CREATE TABLE exam (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    category_id         BIGINT NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    max_time_minutes    INT NOT NULL DEFAULT 60,
    success_percentage  INT NOT NULL DEFAULT 70,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- ExamStatus enum
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_exam_category ON exam(category_id);
CREATE INDEX idx_exam_status   ON exam(status);

-- ---------------------------------------------------------------
-- QUESTION
-- A quiz question belonging to an exam.
-- ---------------------------------------------------------------
CREATE TABLE question (
    id            BIGSERIAL PRIMARY KEY,
    exam_id       BIGINT NOT NULL REFERENCES exam(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    explanation   TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'BETA',   -- QuestionStatus enum
    answer_type   VARCHAR(20) NOT NULL DEFAULT 'RADIO',  -- AnswerType enum
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_question_exam   ON question(exam_id);
CREATE INDEX idx_question_status ON question(status);

-- ---------------------------------------------------------------
-- POSSIBLE_ANSWER
-- One answer option for a question.
-- ---------------------------------------------------------------
CREATE TABLE possible_answer (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    text        TEXT NOT NULL,
    is_correct  BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_answer_question ON possible_answer(question_id);

-- ---------------------------------------------------------------
-- SEED DATA – starter categories for MVP
-- ---------------------------------------------------------------
INSERT INTO category (name, slug, description) VALUES
    ('Java',        'java',        'Core Java, JVM, Collections, Concurrency, JDK features'),
    ('Spring Boot', 'spring-boot', 'Spring Boot, REST, Security, Data JPA, Cloud'),
    ('C++',         'cpp',         'C++ language fundamentals, STL, memory management'),
    ('AI',          'ai',          'Machine Learning, Neural Networks, LLMs, Prompt Engineering');
