export interface Tenant {
  id: string;
  name: string;
  slug: string;
  active: boolean;
  userCount: number;
  createdAt: string;
  whatsappNotificationsEnabled: boolean;
  whatsappMessageCount: number;
  aiChatEnabled: boolean;
  aiChatMessageCount: number;
}

export interface CreateTenantRequest {
  tenantName: string;
  slug: string;
  adminFullName: string;
  adminEmail: string;
  adminPassword: string;
}

export interface UpdateTenantStatusRequest {
  active: boolean;
}

export interface UpdateTenantWhatsappRequest {
  enabled: boolean;
}

export interface UpdateTenantAiChatRequest {
  enabled: boolean;
}

/** Read-only self-service view of the caller's own clinic, returned by
 * GET /api/tenant/me (ADMIN only). */
export interface TenantSelfInfo {
  name: string;
  slug: string;
  active: boolean;
  whatsappNotificationsEnabled: boolean;
  whatsappMessageCount: number;
  aiChatEnabled: boolean;
  aiChatMessageCount: number;
}

export type MessageStatsRange = 'ALL_TIME' | 'THIS_MONTH' | 'LAST_MONTH';

export interface TenantMessageStats {
  range: MessageStatsRange;
  whatsappMessageCount: number;
  aiChatMessageCount: number;
  rangeStart: string;
  rangeEnd: string;
}

/** Returned exactly once by POST /super-admin/tenants/{id}/reset-admin-password.
 * Never persisted or refetchable afterward - copy/relay it immediately. */
export interface TenantAdminPasswordReset {
  adminEmail: string;
  temporaryPassword: string;
}
