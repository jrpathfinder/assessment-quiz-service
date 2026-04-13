-- V7: Align exam_attempt with ITBelts patterns
--   • Add deadline_at  — server-calculated expiry timestamp (startedAt + maxTimeMinutes)
--   • Add percentage   — score * 100 / total, stored for history queries
--   • Widen status     — now stores PASSED | FAILED_LOW_SCORE | FAILED_TIMEOUT | IN_PROGRESS

ALTER TABLE exam_attempt
    ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS percentage  INT;

-- Back-fill deadline_at for any legacy IN_PROGRESS rows (60-min default)
UPDATE exam_attempt
   SET deadline_at = started_at + INTERVAL '60 minutes'
 WHERE deadline_at IS NULL;

-- Make the column NOT NULL going forward (new rows always supply it)
ALTER TABLE exam_attempt
    ALTER COLUMN deadline_at SET NOT NULL;

-- Widen the status column to hold longer enum values
ALTER TABLE exam_attempt
    ALTER COLUMN status TYPE VARCHAR(30);

-- Migrate old SUBMITTED rows: passed=true → PASSED, passed=false → FAILED_LOW_SCORE
UPDATE exam_attempt SET status = 'PASSED'           WHERE status = 'SUBMITTED' AND passed = TRUE;
UPDATE exam_attempt SET status = 'FAILED_LOW_SCORE' WHERE status = 'SUBMITTED' AND passed = FALSE;
