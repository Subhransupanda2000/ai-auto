-- Refresh tokens let a staff session silently renew its short-lived JWT
-- access token instead of forcing a full re-login every
-- app.jwt.access-token-ttl-minutes. Hashed at rest (only the hash is ever
-- persisted), single-use (rotated on every refresh), and revocable - see
-- com.healthcareai.entity.RefreshToken / RefreshTokenServiceImpl.
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
