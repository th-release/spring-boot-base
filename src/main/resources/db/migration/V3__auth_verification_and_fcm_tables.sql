CREATE TABLE IF NOT EXISTS auth_verification (
    id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    target VARCHAR(255) NOT NULL,
    verification_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    metadata VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auth_verification_user_type ON auth_verification (user_uuid, type);

CREATE TABLE IF NOT EXISTS fcm_device_token (
    id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    device_token VARCHAR(512) NOT NULL UNIQUE,
    device_label VARCHAR(120),
    user_agent VARCHAR(512),
    last_ip_address VARCHAR(64),
    last_used_at TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fcm_device_token_user_uuid ON fcm_device_token (user_uuid);
