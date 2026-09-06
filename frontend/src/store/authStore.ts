import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode';
import type { AuthUser, Role } from '../types/auth';

interface JwtClaims {
  sub: string;
  role: Role;
  iss: string;
  iat: number;
  exp: number;
}

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  expiresAt: number | null;
  isAuthenticated: boolean;
  setSession: (accessToken: string) => void;
  logout: () => void;
  isTokenExpired: () => boolean;
}

function decodeUser(token: string): { user: AuthUser; expiresAt: number } {
  const claims = jwtDecode<JwtClaims>(token);
  return {
    user: { email: claims.sub, role: claims.role },
    expiresAt: claims.exp * 1000,
  };
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      expiresAt: null,
      isAuthenticated: false,
      setSession: (accessToken: string) => {
        const { user, expiresAt } = decodeUser(accessToken);
        set({ accessToken, user, expiresAt, isAuthenticated: true });
      },
      logout: () => set({ accessToken: null, user: null, expiresAt: null, isAuthenticated: false }),
      isTokenExpired: () => {
        const { expiresAt } = get();
        return expiresAt == null || Date.now() >= expiresAt;
      },
    }),
    {
      name: 'healthcareai-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        user: state.user,
        expiresAt: state.expiresAt,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
