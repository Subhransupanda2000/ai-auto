import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { superAdminApi } from '../api/superAdminApi';
import type { ApiError } from '../types/common';
import type { CreatePaymentRequest, Payment, PaymentFilters } from '../types/payment';

const paymentsQueryKey = (filters?: PaymentFilters) => ['super-admin', 'payments', filters] as const;

export function useSuperAdminPayments(filters?: PaymentFilters) {
  return useQuery({
    queryKey: paymentsQueryKey(filters),
    queryFn: () => superAdminApi.listPayments(filters),
  });
}

export function useCreatePayment() {
  const queryClient = useQueryClient();
  return useMutation<Payment, ApiError, CreatePaymentRequest>({
    mutationFn: (payload) => superAdminApi.createPayment(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'payments'] }),
  });
}

export function useCancelPayment() {
  const queryClient = useQueryClient();
  return useMutation<Payment, ApiError, string>({
    mutationFn: (id) => superAdminApi.cancelPayment(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'payments'] }),
  });
}
