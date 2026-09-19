import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../api/queryKeys';
import { authApi } from '../api/authApi';
import { userApi, type UserPasswordReset } from '../api/userApi';
import type { RegisterRequest, UserResponse } from '../types/auth';
import type { ApiError } from '../types/common';

export function useStaff(enabled = true) {
  return useQuery({
    queryKey: queryKeys.staff,
    queryFn: () => userApi.list(),
    enabled,
  });
}

export function useInviteStaff() {
  const queryClient = useQueryClient();
  return useMutation<UserResponse, ApiError, RegisterRequest>({
    mutationFn: (payload) => authApi.register(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.staff }),
  });
}

export function useUpdateStaffStatus() {
  const queryClient = useQueryClient();
  return useMutation<UserResponse, ApiError, { id: string; enabled: boolean }>({
    mutationFn: ({ id, enabled }) => userApi.updateStatus(id, { enabled }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.staff }),
  });
}

export function useResetStaffPassword() {
  return useMutation<UserPasswordReset, ApiError, string>({
    mutationFn: (userId) => userApi.resetPassword(userId),
  });
}
