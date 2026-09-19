import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode';
import type { AuthUser, Role } from '../types/auth';

interface JwtClaims {
  sub: string;
  role: Role;
  tenantName?: string;
  iss: string;
  iat: number;
  exp: number;
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: AuthUser | null;
  expiresAt: number | null;
  isAuthenticated: boolean;
  setSession: (accessToken: string, refreshToken: string | null) => void;
  logout: () => void;
  isTokenExpired: () => boolean;
  /** True once the access token is within `thresholdMs` of expiring (or
   * already expired) - used by AuthWatcher to proactively refresh it
   * before a request would otherwise fail with 401. */
  isTokenExpiringSoon: (thresholdMs: number) => boolean;
}

function decodeUser(token: string): { user: AuthUser; expiresAt: number } {
  const claims = jwtDecode<JwtClaims>(token);
  return {
    user: { email: claims.sub, role: claims.role, tenantName: claims.tenantName },
    expiresAt: claims.exp * 1000,
  };
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      expiresAt: null,
      isAuthenticated: false,
      setSession: (accessToken: string, refreshToken: string | null) => {
        const { user, expiresAt } = decodeUser(accessToken);
        set({ accessToken, refreshToken, user, expiresAt, isAuthenticated: true });
      },
      logout: () => set({ accessToken: null, refreshToken: null, user: null, expiresAt: null, isAuthenticated: false }),
      isTokenExpired: () => {
        const { expiresAt } = get();
        return expiresAt == null || Date.now() >= expiresAt;
      },
      isTokenExpiringSoon: (thresholdMs: number) => {
        const { expiresAt } = get();
        return expiresAt == null || Date.now() >= expiresAt - thresholdMs;
      },
    }),
    {
      name: 'healthcareai-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        expiresAt: state.expiresAt,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
