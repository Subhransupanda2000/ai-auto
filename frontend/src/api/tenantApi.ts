import { apiClient } from './client';
import type { MessageStatsRange, TenantMessageStats, TenantSelfInfo } from '../types/tenant';

export const tenantApi = {
  me: () => apiClient.get<TenantSelfInfo>('/tenant/me').then((res) => res.data),

  messageStats: (range: MessageStatsRange) =>
    apiClient.get<TenantMessageStats>('/tenant/me/message-stats', { params: { range } }).then((res) => res.data),
};
