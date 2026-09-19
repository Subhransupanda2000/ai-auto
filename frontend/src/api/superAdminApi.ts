import { superAdminClient } from './superAdminClient';
import type { LoginRequest, LoginResponse } from '../types/auth';
import type {
  CreateTenantRequest,
  MessageStatsRange,
  Tenant,
  TenantAdminPasswordReset,
  UpdateTenantAiChatRequest,
  UpdateTenantStatusRequest,
  UpdateTenantWhatsappRequest,
} from '../types/tenant';
import type { CreatePaymentRequest, Payment, PaymentFilters } from '../types/payment';
import type { Enquiry, EnquiryStatus, UpdateEnquiryStatusRequest } from '../types/enquiry';

export const superAdminApi = {
  login: (payload: LoginRequest) =>
    superAdminClient.post<LoginResponse>('/super-admin/auth/login', payload).then((res) => res.data),

  listTenants: (range: MessageStatsRange = 'ALL_TIME') =>
    superAdminClient.get<Tenant[]>('/super-admin/tenants', { params: { range } }).then((res) => res.data),

  createTenant: (payload: CreateTenantRequest) =>
    superAdminClient.post<Tenant>('/super-admin/tenants', payload).then((res) => res.data),

  updateTenantStatus: (id: string, payload: UpdateTenantStatusRequest) =>
    superAdminClient.patch<Tenant>(`/super-admin/tenants/${id}`, payload).then((res) => res.data),

  updateTenantWhatsapp: (id: string, payload: UpdateTenantWhatsappRequest) =>
    superAdminClient.patch<Tenant>(`/super-admin/tenants/${id}/whatsapp`, payload).then((res) => res.data),

  updateTenantAiChat: (id: string, payload: UpdateTenantAiChatRequest) =>
    superAdminClient.patch<Tenant>(`/super-admin/tenants/${id}/ai-chat`, payload).then((res) => res.data),

  resetAdminPassword: (id: string) =>
    superAdminClient
      .post<TenantAdminPasswordReset>(`/super-admin/tenants/${id}/reset-admin-password`)
      .then((res) => res.data),

  listPayments: (filters?: PaymentFilters) =>
    superAdminClient.get<Payment[]>('/super-admin/payments', { params: filters }).then((res) => res.data),

  createPayment: (payload: CreatePaymentRequest) =>
    superAdminClient.post<Payment>('/super-admin/payments', payload).then((res) => res.data),

  cancelPayment: (id: string) =>
    superAdminClient.delete<Payment>(`/super-admin/payments/${id}`).then((res) => res.data),

  listEnquiries: (status?: EnquiryStatus) =>
    superAdminClient.get<Enquiry[]>('/super-admin/enquiries', { params: { status } }).then((res) => res.data),

  updateEnquiryStatus: (id: string, payload: UpdateEnquiryStatusRequest) =>
    superAdminClient.patch<Enquiry>(`/super-admin/enquiries/${id}/status`, payload).then((res) => res.data),
};
