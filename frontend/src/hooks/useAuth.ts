import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest } from '../types/auth';
import type { ApiError } from '../types/common';

export function useAuth() {
  const { user, isAuthenticated, setSession, logout } = useAuthStore();
  const navigate = useNavigate();

  const loginMutation = useMutation<void, ApiError, LoginRequest>({
    mutationFn: async (payload) => {
      const response = await authApi.login(payload);
      setSession(response.accessToken);
    },
    onSuccess: () => navigate('/', { replace: true }),
  });

  const signOut = () => {
    logout();
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
