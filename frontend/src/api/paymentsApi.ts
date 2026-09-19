import { apiClient } from './client';
import type { Payment, PaymentCheckoutResponse, PaymentFilters, PaymentVerifyRequest } from '../types/payment';

export const paymentsApi = {
  list: (filters?: PaymentFilters) =>
    apiClient.get<Payment[]>('/tenant/payments', { params: filters }).then((res) => res.data),

  checkout: (id: string) =>
    apiClient.post<PaymentCheckoutResponse>(`/tenant/payments/${id}/checkout`).then((res) => res.data),

  verify: (id: string, payload: PaymentVerifyRequest) =>
    apiClient.post<Payment>(`/tenant/payments/${id}/verify`, payload).then((res) => res.data),
};
