CREATE TABLE question (
                          id BIGSERIAL PRIMARY KEY,
    -- domain
                          language VARCHAR(50) NOT NULL,
                          topic VARCHAR(100) NOT NULL,
                          difficulty VARCHAR(20) NOT NULL,
    -- content
                          question_text TEXT NOT NULL,
                          answer_text TEXT,
                          explanation TEXT,
    -- metadata
                          source VARCHAR(20) NOT NULL,
                          tags TEXT,                           -- stored as comma-separated; dropped in V2
    -- scoring / usage
                          times_used INT DEFAULT 0,
                          success_rate FLOAT DEFAULT 0.0,
    -- timestamps
                          created_at TIMESTAMP DEFAULT NOW(),
                          updated_at TIMESTAMP DEFAULT NOW()
);
