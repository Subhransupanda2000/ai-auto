-- Multi-tenancy: each clinic is a Tenant. All clinic data (doctors,
-- patients, appointments, conversations, faq_documents) and staff users
-- are scoped to exactly one tenant via tenant_id, enforced at the
-- application layer via Hibernate's @TenantId partitioned multitenancy
-- (see com.healthcareai.tenant.TenantContext /
-- com.healthcareai.tenant.TenantIdentifierResolver).
--
-- Platform-level operators who onboard tenants live in a completely
-- separate `super_admins` table with their own login flow: they have no
-- tenant_id and never see clinic data. Only a super admin can create a
-- tenant (and its first ADMIN user).
--
-- This is a breaking schema change (tenant_id is NOT NULL on clinic tables,
-- with no sensible backfill value for pre-existing rows), so existing
-- demo/staff data is cleared here rather than migrated - acceptable
-- pre-production, per project decision.

TRUNCATE TABLE audit_logs, conversations, appointments, patients, doctors, faq_documents, users
    RESTART IDENTITY CASCADE;

CREATE TABLE tenants (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    slug         VARCHAR(100) NOT NULL UNIQUE,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE super_admins (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE users ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE;
CREATE INDEX idx_users_tenant_id ON users (tenant_id);

ALTER TABLE doctors ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE;
CREATE INDEX idx_doctors_tenant_id ON doctors (tenant_id);

-- A phone number only needs to be unique within a tenant: the same person
-- may be a patient at more than one (unrelated) clinic.
ALTER TABLE patients ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE;
ALTER TABLE patients DROP CONSTRAINT patients_phone_number_key;
CREATE UNIQUE INDEX uq_patients_tenant_phone ON patients (tenant_id, phone_number);
CREATE INDEX idx_patients_tenant_id ON patients (tenant_id);

ALTER TABLE appointments ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE;
CREATE INDEX idx_appointments_tenant_id ON appointments (tenant_id);

ALTER TABLE conversations ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE;
CREATE INDEX idx_conversations_tenant_id ON conversations (tenant_id);

ALTER TABLE faq_documents ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE;
CREATE INDEX idx_faq_documents_tenant_id ON faq_documents (tenant_id);

-- Audit logs may also record platform-level (super admin) actions that
-- aren't scoped to any tenant, so tenant_id stays nullable here and is
-- NOT part of the automatic Hibernate tenant filter.
ALTER TABLE audit_logs ADD COLUMN tenant_id UUID REFERENCES tenants (id) ON DELETE CASCADE;
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs (tenant_id);
