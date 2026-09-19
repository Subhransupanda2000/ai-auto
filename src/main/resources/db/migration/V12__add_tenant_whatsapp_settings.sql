-- Per-tenant WhatsApp appointment-notification feature toggle + a running
-- count of messages sent, both managed by a super admin from the Tenants
-- page. See com.healthcareai.entity.Tenant / AppointmentServiceImpl.

ALTER TABLE tenants
    ADD COLUMN whatsapp_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE tenants
    ADD COLUMN whatsapp_message_count BIGINT NOT NULL DEFAULT 0;
