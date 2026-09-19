-- Sales leads raised via the public "Request a Demo" form. See
-- com.healthcareai.entity.Enquiry.
CREATE TABLE enquiries (
    id          UUID PRIMARY KEY,
    full_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(50),
    clinic_name VARCHAR(255),
    message     VARCHAR(2000),
    status      VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_enquiries_status ON enquiries (status);
CREATE INDEX idx_enquiries_created_at ON enquiries (created_at);
