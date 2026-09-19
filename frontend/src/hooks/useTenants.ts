import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { superAdminApi } from '../api/superAdminApi';
import type { ApiError } from '../types/common';
import type { CreateTenantRequest, MessageStatsRange, Tenant, TenantAdminPasswordReset } from '../types/tenant';

const tenantsQueryKey = (range: MessageStatsRange) => ['super-admin', 'tenants', range] as const;

/** @param range Which window whatsappMessageCount/aiChatMessageCount on
 * each returned tenant should reflect - ALL_TIME (default), THIS_MONTH, or
 * LAST_MONTH. Drives the message-count dropdown on the Tenants page. */
export function useTenants(range: MessageStatsRange = 'ALL_TIME') {
  return useQuery({
    queryKey: tenantsQueryKey(range),
    queryFn: () => superAdminApi.listTenants(range),
  });
}

export function useCreateTenant() {
  const queryClient = useQueryClient();
  return useMutation<Tenant, ApiError, CreateTenantRequest>({
    mutationFn: (payload) => superAdminApi.createTenant(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'tenants'] }),
  });
}

export function useUpdateTenantStatus() {
  const queryClient = useQueryClient();
  return useMutation<Tenant, ApiError, { id: string; active: boolean }>({
    mutationFn: ({ id, active }) => superAdminApi.updateTenantStatus(id, { active }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'tenants'] }),
  });
}

export function useUpdateTenantWhatsapp() {
  const queryClient = useQueryClient();
  return useMutation<Tenant, ApiError, { id: string; enabled: boolean }>({
    mutationFn: ({ id, enabled }) => superAdminApi.updateTenantWhatsapp(id, { enabled }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'tenants'] }),
  });
}

export function useUpdateTenantAiChat() {
  const queryClient = useQueryClient();
  return useMutation<Tenant, ApiError, { id: string; enabled: boolean }>({
    mutationFn: ({ id, enabled }) => superAdminApi.updateTenantAiChat(id, { enabled }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'tenants'] }),
  });
}

/** Generates and sets a new temporary password for a clinic's admin
 * account, returned once by the mutation's onSuccess - see
 * ResetAdminPasswordDialog for the one-time reveal + copy UI. */
export function useResetTenantAdminPassword() {
  return useMutation<TenantAdminPasswordReset, ApiError, string>({
    mutationFn: (tenantId) => superAdminApi.resetAdminPassword(tenantId),
  });
}
