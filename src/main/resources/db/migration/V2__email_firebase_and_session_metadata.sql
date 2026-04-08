ALTER TABLE IF EXISTS auth_entity
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_code_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_code_expiry TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_auth_entity_email ON auth_entity (email);

ALTER TABLE IF EXISTS refresh_token_entity
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(512),
    ADD COLUMN IF NOT EXISTS device_label VARCHAR(128),
    ADD COLUMN IF NOT EXISTS ip_address VARCHAR(64),
    ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;
