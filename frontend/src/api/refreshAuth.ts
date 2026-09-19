import axios from 'axios';
import { API_BASE_URL } from './env';
import { useAuthStore } from '../store/authStore';

let inFlightRefresh: Promise<string | null> | null = null;

/**
 * Exchanges the stored refresh token for a new access+refresh token pair,
 * updating `useAuthStore` in place. Concurrent callers (e.g. several
 * requests failing with 401 at once, or the interceptor and AuthWatcher's
 * proactive check racing) share a single in-flight request instead of each
 * hitting `/auth/refresh` separately - refresh tokens are single-use, so a
 * second concurrent call would otherwise fail as "already used".
 *
 * Uses a bare `axios` call (not `apiClient`) so this never re-enters
 * `apiClient`'s own interceptors.
 *
 * @returns the new access token, or `null` if there's no refresh token to
 * use or the exchange failed (expired/revoked) - callers should treat
 * `null` as "the user needs to log in again".
 */
export function refreshAccessToken(): Promise<string | null> {
  if (!inFlightRefresh) {
    inFlightRefresh = performRefresh().finally(() => {
      inFlightRefresh = null;
    });
  }
  return inFlightRefresh;
}

async function performRefresh(): Promise<string | null> {
  const refreshToken = useAuthStore.getState().refreshToken;
  if (!refreshToken) {
    return null;
  }
  try {
    const response = await axios.post<{ accessToken: string; refreshToken: string | null }>(
      `${API_BASE_URL}/auth/refresh`,
      { refreshToken },
    );
    useAuthStore.getState().setSession(response.data.accessToken, response.data.refreshToken);
    return response.data.accessToken;
  } catch {
    return null;
  }
}
