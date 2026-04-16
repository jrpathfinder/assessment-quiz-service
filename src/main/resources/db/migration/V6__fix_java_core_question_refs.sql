-- V4 hardcoded exam_id=1 but 'Java Core' was assigned id=2 at runtime.
-- Move all questions from 'First Java Core Exam' to 'Java Core'.
UPDATE question
SET exam_id = (SELECT id FROM exam WHERE name = 'Java Core')
WHERE exam_id = (SELECT id FROM exam WHERE name = 'First Java Core Exam');
