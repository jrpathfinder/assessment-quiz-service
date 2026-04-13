-- V10: Create app_user table for authentication
CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),                   -- null for OAuth2-only users
    google_sub    VARCHAR(255) UNIQUE,             -- Google OAuth2 subject claim
    display_name  VARCHAR(255),
    role          VARCHAR(50)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_user_email     ON app_user (email);
CREATE INDEX idx_app_user_google_sub ON app_user (google_sub);
