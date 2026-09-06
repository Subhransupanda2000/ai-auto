CREATE TABLE patients (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    email             VARCHAR(255),
    phone_number      VARCHAR(32)  NOT NULL UNIQUE,
    date_of_birth     DATE,
    gender            VARCHAR(20),
    notes             TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_patients_phone_number ON patients (phone_number);
CREATE INDEX idx_patients_email ON patients (email);
