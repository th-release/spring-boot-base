CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_uuid VARCHAR(36),
    action VARCHAR(120) NOT NULL,
    resource_type VARCHAR(120) NOT NULL,
    resource_id VARCHAR(120),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    client_ip VARCHAR(64),
    user_agent VARCHAR(512),
    detail TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

ALTER TABLE IF EXISTS auth_entity
    ADD COLUMN IF NOT EXISTS failed_login_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(64),
    ADD COLUMN IF NOT EXISTS mfa_secret TEXT,
    ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE IF EXISTS refresh_token_entity
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(1024),
    ADD COLUMN IF NOT EXISTS token_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS family_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS replaced_by_token_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS user_uuid VARCHAR(36);

CREATE UNIQUE INDEX IF NOT EXISTS uk_refresh_token_entity_token_id ON refresh_token_entity (token_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_entity_user_uuid ON refresh_token_entity (user_uuid);
CREATE INDEX IF NOT EXISTS idx_refresh_token_entity_family_id ON refresh_token_entity (family_id);
