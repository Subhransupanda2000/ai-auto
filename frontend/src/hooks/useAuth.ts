import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest } from '../types/auth';
import type { ApiError } from '../types/common';

export function useAuth() {
  const { user, isAuthenticated, refreshToken, setSession, logout } = useAuthStore();
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
      setSession(response.accessToken, response.refreshToken);
    },
    onSuccess: () => navigate('/', { replace: true }),
  });

  const signOut = () => {
    // Best-effort: revoke the refresh token server-side too, so it can't
    // be used to silently renew a session after the user explicitly signs
    // out. Never blocks the local sign-out on this - an expired/missing
    // token, or the request itself failing, still leaves the user logged
    // out locally.
    if (refreshToken) {
      authApi.logout({ refreshToken }).catch(() => undefined);
    }
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
