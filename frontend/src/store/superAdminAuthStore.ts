import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode';

interface SuperAdminJwtClaims {
  sub: string;
  iss: string;
  iat: number;
  exp: number;
}

interface SuperAdminAuthState {
  accessToken: string | null;
  email: string | null;
  expiresAt: number | null;
  isAuthenticated: boolean;
  setSession: (accessToken: string) => void;
  logout: () => void;
  isTokenExpired: () => boolean;
}

/**
 * Entirely separate from {@code useAuthStore}: the super-admin platform
 * login has its own token, storage key, and session lifecycle, so a staff
 * session and a super-admin session can coexist (e.g. two browser tabs)
 * without interfering with each other.
 */
export const useSuperAdminAuthStore = create<SuperAdminAuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      email: null,
      expiresAt: null,
      isAuthenticated: false,
      setSession: (accessToken: string) => {
        const claims = jwtDecode<SuperAdminJwtClaims>(accessToken);
        set({ accessToken, email: claims.sub, expiresAt: claims.exp * 1000, isAuthenticated: true });
      },
      logout: () => set({ accessToken: null, email: null, expiresAt: null, isAuthenticated: false }),
      isTokenExpired: () => {
        const { expiresAt } = get();
        return expiresAt == null || Date.now() >= expiresAt;
      },
    }),
    {
      name: 'healthcareai-super-admin-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        email: state.email,
        expiresAt: state.expiresAt,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
