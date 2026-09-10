import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { superAdminApi } from '../api/superAdminApi';
import type { ApiError } from '../types/common';
import type { CreateTenantRequest, Tenant } from '../types/tenant';

const tenantsQueryKey = ['super-admin', 'tenants'] as const;

export function useTenants() {
  return useQuery({
    queryKey: tenantsQueryKey,
    queryFn: () => superAdminApi.listTenants(),
  });
}

export function useCreateTenant() {
  const queryClient = useQueryClient();
  return useMutation<Tenant, ApiError, CreateTenantRequest>({
    mutationFn: (payload) => superAdminApi.createTenant(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: tenantsQueryKey }),
  });
}

export function useUpdateTenantStatus() {
  const queryClient = useQueryClient();
  return useMutation<Tenant, ApiError, { id: string; active: boolean }>({
    mutationFn: ({ id, active }) => superAdminApi.updateTenantStatus(id, { active }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: tenantsQueryKey }),
  });
}
