import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { paymentsApi } from '../api/paymentsApi';
import type { ApiError } from '../types/common';
import type { Payment, PaymentCheckoutResponse, PaymentFilters, PaymentVerifyRequest } from '../types/payment';

const paymentsQueryKey = (filters?: PaymentFilters) => ['tenant', 'payments', filters] as const;

export function usePayments(filters?: PaymentFilters) {
  return useQuery({
    queryKey: paymentsQueryKey(filters),
    queryFn: () => paymentsApi.list(filters),
  });
}

export function useCheckoutPayment() {
  return useMutation<PaymentCheckoutResponse, ApiError, string>({
    mutationFn: (id) => paymentsApi.checkout(id),
  });
}

export function useVerifyPayment() {
  const queryClient = useQueryClient();
  return useMutation<Payment, ApiError, { id: string; payload: PaymentVerifyRequest }>({
    mutationFn: ({ id, payload }) => paymentsApi.verify(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tenant', 'payments'] }),
  });
}
