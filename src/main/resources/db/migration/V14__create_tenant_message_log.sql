-- Append-only per-message log backing "this month" / "last month" message
-- volume breakdowns for the WhatsApp appointment-notification and AI
-- receptionist chat features (see com.healthcareai.entity.TenantMessageLog
-- / TenantMessageLogService). Tenant.whatsapp_message_count and
-- ai_chat_message_count (added in V12/V13) remain the lifetime totals;
-- this table lets us re-derive counts for any date range without altering
-- those.

CREATE TABLE tenant_message_log (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  UUID        NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    channel    VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_message_log_tenant_channel_created
    ON tenant_message_log (tenant_id, channel, created_at);
