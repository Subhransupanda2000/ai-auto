-- Per-tenant AI receptionist chat feature toggle + a running count of
-- messages handled, both managed by a super admin from the Tenants page
-- (and readable by the clinic's own admin via GET /api/tenant/me). See
-- com.healthcareai.entity.Tenant / ChatController.

ALTER TABLE tenants
    ADD COLUMN ai_chat_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE tenants
    ADD COLUMN ai_chat_message_count BIGINT NOT NULL DEFAULT 0;
