CREATE TABLE appointments (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id            UUID NOT NULL REFERENCES patients (id) ON DELETE CASCADE,
    doctor_id             UUID NOT NULL REFERENCES doctors (id) ON DELETE RESTRICT,
    scheduled_start        TIMESTAMPTZ NOT NULL,
    scheduled_end           TIMESTAMPTZ NOT NULL,
    status                VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED'
                              CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'RESCHEDULED', 'NO_SHOW')),
    reason                VARCHAR(255),
    notes                 TEXT,
    reminder_sent_at      TIMESTAMPTZ,
    confirmation_sent_at  TIMESTAMPTZ,
    google_calendar_event_id VARCHAR(255),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (scheduled_end > scheduled_start)
);

CREATE INDEX idx_appointments_patient_id ON appointments (patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments (doctor_id);
CREATE INDEX idx_appointments_scheduled_start ON appointments (scheduled_start);
CREATE INDEX idx_appointments_status ON appointments (status);

-- Prevent double-booking the same doctor for the same start time, unless the
-- existing appointment has been cancelled.
CREATE UNIQUE INDEX uq_appointments_doctor_slot
    ON appointments (doctor_id, scheduled_start)
    WHERE status NOT IN ('CANCELLED');
