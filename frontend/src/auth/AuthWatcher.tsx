import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { queryClient } from '../lib/queryClient';

const CHECK_INTERVAL_MS = 30_000;

/**
 * Periodically checks whether the stored JWT has expired and, if so, logs
 * the user out and redirects to the login page. This handles the "auto
 * logout" requirement without requiring a refresh-token endpoint (the
 * backend does not currently issue refresh tokens).
 */
export function AuthWatcher() {
  const navigate = useNavigate();

  useEffect(() => {
    const check = () => {
      const { isAuthenticated, isTokenExpired, logout } = useAuthStore.getState();
      if (isAuthenticated && isTokenExpired()) {
        logout();
        // See useAuth's signOut/login for why this matters: without it, the
        // next person to log in in this tab could momentarily see (and even
        // submit against) the previous session's cached patients/doctors/
        // appointments.
        queryClient.clear();
        navigate('/login', { replace: true });
      }
    };
    const interval = setInterval(check, CHECK_INTERVAL_MS);
    check();
    return () => clearInterval(interval);
  }, [navigate]);

  return null;
}
