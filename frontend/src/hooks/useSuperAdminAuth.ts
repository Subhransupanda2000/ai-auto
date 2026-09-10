import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { superAdminApi } from '../api/superAdminApi';
import { useSuperAdminAuthStore } from '../store/superAdminAuthStore';
import type { LoginRequest } from '../types/auth';
import type { ApiError } from '../types/common';

export function useSuperAdminAuth() {
  const { email, isAuthenticated, setSession, logout } = useSuperAdminAuthStore();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const loginMutation = useMutation<void, ApiError, LoginRequest>({
    mutationFn: async (payload) => {
      const response = await superAdminApi.login(payload);
      queryClient.clear();
      setSession(response.accessToken);
    },
    onSuccess: () => navigate('/super-admin/tenants', { replace: true }),
  });

  const signOut = () => {
    logout();
    queryClient.clear();
    navigate('/super-admin/login', { replace: true });
  };

  return {
    email,
    isAuthenticated,
    login: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,
    logout: signOut,
  };
}
