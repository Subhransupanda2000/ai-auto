import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { queryClient } from '../lib/queryClient';
import { refreshAccessToken } from '../api/refreshAuth';

const CHECK_INTERVAL_MS = 30_000;
// Refresh a bit before the access token actually expires so an in-flight
// request never races the expiry itself.
const REFRESH_THRESHOLD_MS = 2 * 60_000;

/**
 * Periodically checks whether the stored JWT access token is about to
 * expire and, if so, silently exchanges the refresh token for a new one
 * (see `api/refreshAuth.ts`) - keeping the user signed in for as long as
 * their refresh token is valid (`app.jwt.refresh-token-ttl-days`, default
 * 7 days) instead of forcing a full re-login every access-token TTL.
 * Only actually logs the user out if that exchange fails (refresh token
 * itself expired/revoked, or missing entirely).
 */
export function AuthWatcher() {
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;

    const check = async () => {
      const { isAuthenticated, isTokenExpiringSoon, logout } = useAuthStore.getState();
      if (!isAuthenticated || !isTokenExpiringSoon(REFRESH_THRESHOLD_MS)) {
        return;
      }

      const newAccessToken = await refreshAccessToken();
      if (cancelled || newAccessToken) {
        return;
      }

      logout();
      // See useAuth's signOut/login for why this matters: without it, the
      // next person to log in in this tab could momentarily see (and even
      // submit against) the previous session's cached patients/doctors/
      // appointments.
      queryClient.clear();
      navigate('/login', { replace: true });
    };

    const interval = setInterval(check, CHECK_INTERVAL_MS);
    check();
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [navigate]);

  return null;
}
