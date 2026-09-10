import { useMutation } from '@tanstack/react-query';
import { authApi } from '../api/authApi';
import type { ChangePasswordRequest, ForgotPasswordRequest, MessageResponse, ResetPasswordRequest } from '../types/auth';
import type { ApiError } from '../types/common';

export function useForgotPassword() {
  return useMutation<MessageResponse, ApiError, ForgotPasswordRequest>({
    mutationFn: (payload) => authApi.forgotPassword(payload),
  });
}

export function useResetPassword() {
  return useMutation<MessageResponse, ApiError, ResetPasswordRequest>({
    mutationFn: (payload) => authApi.resetPassword(payload),
  });
}

export function useChangePassword() {
  return useMutation<MessageResponse, ApiError, ChangePasswordRequest>({
    mutationFn: (payload) => authApi.changePassword(payload),
  });
}
