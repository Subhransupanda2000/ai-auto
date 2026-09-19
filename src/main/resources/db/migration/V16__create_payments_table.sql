-- Invoices raised by a super admin against a clinic tenant, collected
-- through Razorpay Checkout. See com.healthcareai.entity.Payment.
CREATE TABLE payments (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID           NOT NULL REFERENCES tenants (id),
    amount              NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    description         VARCHAR(500),
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_by          VARCHAR(255)   NOT NULL,
    razorpay_order_id   VARCHAR(64),
    razorpay_payment_id VARCHAR(64),
    razorpay_signature  VARCHAR(255),
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_tenant_id ON payments (tenant_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_created_at ON payments (created_at);
