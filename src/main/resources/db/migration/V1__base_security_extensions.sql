CREATE TABLE IF NOT EXISTS tb_auth (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(24) NOT NULL,
    nickname VARCHAR(36) NOT NULL,
    email VARCHAR(255),
    password TEXT NOT NULL,
    salt VARCHAR(32) NOT NULL,
    password_reset_code_hash VARCHAR(255),
    password_reset_code_expiry TIMESTAMP,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(64),
    mfa_secret TEXT,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_auth_username ON tb_auth (username);
CREATE INDEX IF NOT EXISTS idx_tb_auth_email ON tb_auth (email);

CREATE TABLE IF NOT EXISTS tb_refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    token_id VARCHAR(64) NOT NULL,
    family_id VARCHAR(64) NOT NULL,
    token_hash VARCHAR(1024) NOT NULL,
    token VARCHAR(1024) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    user_agent VARCHAR(512),
    device_label VARCHAR(128),
    ip_address VARCHAR(64),
    last_used_at TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_token_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_refresh_token_user_uuid ON tb_refresh_token (user_uuid);
CREATE INDEX IF NOT EXISTS idx_tb_refresh_token_family_id ON tb_refresh_token (family_id);
CREATE INDEX IF NOT EXISTS idx_tb_refresh_token_token_id ON tb_refresh_token (token_id);

CREATE TABLE IF NOT EXISTS tb_audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_uuid VARCHAR(36),
    action VARCHAR(120) NOT NULL,
    resource_type VARCHAR(120) NOT NULL,
    resource_id VARCHAR(120),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    client_ip VARCHAR(64),
    user_agent VARCHAR(512),
    detail TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_auth_verification (
    id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    target VARCHAR(255) NOT NULL,
    verification_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    metadata VARCHAR(120),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_auth_verification_user_type ON tb_auth_verification (user_uuid, type);

CREATE TABLE IF NOT EXISTS tb_fcm_device_token (
    id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    device_token VARCHAR(512) NOT NULL,
    device_label VARCHAR(120),
    user_agent VARCHAR(512),
    last_ip_address VARCHAR(64),
    last_used_at TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_fcm_device_token_user_uuid ON tb_fcm_device_token (user_uuid);
CREATE INDEX IF NOT EXISTS idx_tb_fcm_device_token_device_token ON tb_fcm_device_token (device_token);

CREATE TABLE IF NOT EXISTS tb_fcm_notification (
    id BIGSERIAL PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    message_id VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    data TEXT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_fcm_notification_user_uuid ON tb_fcm_notification (user_uuid);
CREATE INDEX IF NOT EXISTS idx_tb_fcm_notification_read ON tb_fcm_notification (read);

CREATE TABLE IF NOT EXISTS tb_files (
    id BIGSERIAL PRIMARY KEY,
    file_path VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    file_size BIGINT,
    dir_name VARCHAR(255) NOT NULL,
    owner_uuid VARCHAR(36),
    storage_type VARCHAR(20) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tb_files_owner_uuid ON tb_files (owner_uuid);
