import { superAdminClient } from './superAdminClient';
import type { LoginRequest, LoginResponse } from '../types/auth';
import type { CreateTenantRequest, Tenant, UpdateTenantStatusRequest } from '../types/tenant';

export const superAdminApi = {
  login: (payload: LoginRequest) =>
    superAdminClient.post<LoginResponse>('/super-admin/auth/login', payload).then((res) => res.data),

  listTenants: () => superAdminClient.get<Tenant[]>('/super-admin/tenants').then((res) => res.data),

  createTenant: (payload: CreateTenantRequest) =>
    superAdminClient.post<Tenant>('/super-admin/tenants', payload).then((res) => res.data),

  updateTenantStatus: (id: string, payload: UpdateTenantStatusRequest) =>
    superAdminClient.patch<Tenant>(`/super-admin/tenants/${id}`, payload).then((res) => res.data),
};
