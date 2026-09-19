import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '../api/queryKeys';
import { tenantApi } from '../api/tenantApi';
import { useAuth } from './useAuth';
import type { MessageStatsRange } from '../types/tenant';

/** Read-only view of the caller's own clinic's feature toggles/usage
 * counters (WhatsApp + AI chat), only reachable by an ADMIN - see
 * TenantSelfController. Disabled entirely for non-admins so we never even
 * attempt the request and surface a 403. */
export function useTenantSelf() {
  const { user } = useAuth();
  return useQuery({
    queryKey: queryKeys.tenantSelf,
    queryFn: () => tenantApi.me(),
    enabled: user?.role === 'ADMIN',
  });
}

/** This-month/last-month WhatsApp + AI-chat message counts for the
 * caller's own clinic, ADMIN only (see TenantSelfController.messageStats). */
export function useTenantSelfMessageStats(range: MessageStatsRange) {
  const { user } = useAuth();
  return useQuery({
    queryKey: queryKeys.tenantSelfMessageStats(range),
    queryFn: () => tenantApi.messageStats(range),
    enabled: user?.role === 'ADMIN',
  });
}
