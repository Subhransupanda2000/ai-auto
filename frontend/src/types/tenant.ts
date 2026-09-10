export interface Tenant {
  id: string;
  name: string;
  slug: string;
  active: boolean;
  userCount: number;
  createdAt: string;
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
