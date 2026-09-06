CREATE TABLE doctors (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID REFERENCES users (id) ON DELETE SET NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    specialty         VARCHAR(150) NOT NULL,
    email             VARCHAR(255) UNIQUE,
    phone_number      VARCHAR(32),
    bio               TEXT,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_doctors_specialty ON doctors (specialty);
CREATE INDEX idx_doctors_active ON doctors (active);
