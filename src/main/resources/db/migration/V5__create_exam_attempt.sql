-- Stores one attempt by a user (or anonymous session) at an exam
CREATE TABLE exam_attempt (
    id                  BIGSERIAL PRIMARY KEY,
    exam_id             BIGINT NOT NULL REFERENCES exam(id) ON DELETE CASCADE,
    started_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    submitted_at        TIMESTAMP,
    score               INT,          -- number of correct questions
    total               INT,          -- total questions in this attempt
    passed              BOOLEAN,
    status              VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'  -- IN_PROGRESS | SUBMITTED
);

CREATE INDEX idx_attempt_exam ON exam_attempt(exam_id);

-- One row per question per attempt, storing which answers the user picked
CREATE TABLE attempt_answer (
    id              BIGSERIAL PRIMARY KEY,
    attempt_id      BIGINT NOT NULL REFERENCES exam_attempt(id) ON DELETE CASCADE,
    question_id     BIGINT NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    selected_ids    TEXT NOT NULL DEFAULT ''   -- comma-separated possible_answer ids
);

CREATE INDEX idx_attempt_answer_attempt ON attempt_answer(attempt_id);
