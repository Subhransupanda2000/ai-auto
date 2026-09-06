import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

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
        navigate('/login', { replace: true });
      }
    };
    const interval = setInterval(check, CHECK_INTERVAL_MS);
    check();
    return () => clearInterval(interval);
  }, [navigate]);

  return null;
}
