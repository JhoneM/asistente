-- V1 Schema inicial HabitPet

CREATE TABLE users (
    id               VARCHAR(36)  PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    hashed_password  VARCHAR(255) NOT NULL,
    display_name     VARCHAR(100) NOT NULL,
    timezone         VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE habits (
    id               VARCHAR(36)  PRIMARY KEY,
    user_id          VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name             VARCHAR(100) NOT NULL,
    description      VARCHAR(500),
    category         VARCHAR(20)  NOT NULL CHECK (category IN ('HEALTH','STUDY','SPORT','WELLNESS','NUTRITION')),
    weekly_frequency INT          NOT NULL CHECK (weekly_frequency BETWEEN 1 AND 7),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_habits_user_active ON habits (user_id, is_active);

CREATE TABLE completion_records (
    id         VARCHAR(36) PRIMARY KEY,
    habit_id   VARCHAR(36) NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    user_id    VARCHAR(36) NOT NULL,
    date       DATE        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_completion_records_habit_date UNIQUE (habit_id, date)
);
CREATE INDEX idx_records_user_date ON completion_records (user_id, date);

CREATE TABLE pets (
    id           VARCHAR(36)  PRIMARY KEY,
    user_id      VARCHAR(36)  NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    pet_name     VARCHAR(50)  NOT NULL DEFAULT 'Mi Mascota',
    pet_type     VARCHAR(20)  NOT NULL DEFAULT 'CAT' CHECK (pet_type IN ('CAT','DOG','DRAGON')),
    state        VARCHAR(20)  NOT NULL DEFAULT 'NEUTRAL' CHECK (state IN ('CRITICAL','POOR','NEUTRAL','GOOD','EXCELLENT')),
    xp           INT          NOT NULL DEFAULT 0 CHECK (xp >= 0),
    level        INT          NOT NULL DEFAULT 1 CHECK (level BETWEEN 1 AND 10),
    last_updated TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
    id         VARCHAR(36)  PRIMARY KEY,
    user_id    VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(30)  NOT NULL CHECK (type IN ('PET_CRITICAL','PET_LEVEL_UP','STREAK_MILESTONE')),
    message    VARCHAR(500) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read);

CREATE TABLE refresh_tokens (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
