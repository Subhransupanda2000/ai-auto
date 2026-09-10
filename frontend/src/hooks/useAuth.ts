import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest } from '../types/auth';
import type { ApiError } from '../types/common';

export function useAuth() {
  const { user, isAuthenticated, setSession, logout } = useAuthStore();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const loginMutation = useMutation<void, ApiError, LoginRequest>({
    mutationFn: async (payload) => {
      const response = await authApi.login(payload);
      // Drop any cached data from a previous session before adopting the
      // new one - otherwise a different user/tenant's cached patients,
      // doctors, and appointments can briefly (or not-so-briefly) show up
      // mixed in with this login's own data.
      queryClient.clear();
      setSession(response.accessToken);
    },
    onSuccess: () => navigate('/', { replace: true }),
  });

  const signOut = () => {
    logout();
    queryClient.clear();
    navigate('/login', { replace: true });
  };

  return {
    user,
    isAuthenticated,
    login: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error,
    logout: signOut,
  };
}
