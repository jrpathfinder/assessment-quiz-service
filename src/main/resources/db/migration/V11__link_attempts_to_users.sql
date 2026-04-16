-- V11: Link exam attempts to authenticated users
ALTER TABLE exam_attempt
    ADD COLUMN user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL;

CREATE INDEX idx_exam_attempt_user_id ON exam_attempt (user_id);
