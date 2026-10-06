-- auth_db schema, version 1. NEVER edit this file after it has been merged: add V3__..., V4__... instead.

CREATE TABLE roles (
    id   SMALLINT    NOT NULL,
    name VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE users (
    id                    BINARY(16)   NOT NULL,
    email                 VARCHAR(254) NOT NULL,            -- stored lowercase
    password_hash         VARCHAR(100) NOT NULL,
    full_name             VARCHAR(120) NOT NULL,
    phone                 VARCHAR(20)  NULL,
    status                VARCHAR(20)  NOT NULL,            -- ACTIVE / DISABLED
    email_verified_at     DATETIME(6)  NULL,                -- NULL = not verified yet
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          DATETIME(6)  NULL,
    created_at            DATETIME(6)  NOT NULL,            -- UTC
    updated_at            DATETIME(6)  NOT NULL,            -- UTC
    version               BIGINT       NOT NULL DEFAULT 0,  -- optimistic locking
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id SMALLINT   NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;

CREATE TABLE refresh_tokens (
    id          BINARY(16)   NOT NULL,
    user_id     BINARY(16)   NOT NULL,
    family_id   BINARY(16)   NOT NULL,                      -- all rotated descendants share one family
    token_hash  VARCHAR(64)  NOT NULL,                      -- SHA-256 hex of the token; the token itself is never stored
    issued_at   DATETIME(6)  NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    revoked_at  DATETIME(6)  NULL,
    replaced_by BINARY(16)   NULL,                          -- set when this token was rotated
    user_agent  VARCHAR(255) NULL,
    ip_address  VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_user (user_id),
    INDEX idx_refresh_tokens_family (family_id)
) ENGINE = InnoDB;
