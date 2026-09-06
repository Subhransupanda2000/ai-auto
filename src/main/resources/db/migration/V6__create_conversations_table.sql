CREATE TABLE conversations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id    VARCHAR(100) NOT NULL,
    patient_id    UUID REFERENCES patients (id) ON DELETE SET NULL,
    channel       VARCHAR(32)  NOT NULL DEFAULT 'WEB'
                     CHECK (channel IN ('WEB', 'WHATSAPP', 'SMS', 'EMAIL')),
    role          VARCHAR(16)  NOT NULL
                     CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
    message       TEXT         NOT NULL,
    metadata       JSONB,
    escalated     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversations_session_id ON conversations (session_id, created_at);
CREATE INDEX idx_conversations_patient_id ON conversations (patient_id);
