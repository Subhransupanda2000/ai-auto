import type { ReactElement } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

interface ProtectedRouteProps {
  children: ReactElement;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();

  // Deliberately doesn't also check isTokenExpired(): an expired access
  // token with a still-valid refresh token is a normal state (e.g. right
  // after opening the app after being away) - AuthWatcher silently renews
  // it, and apiClient's interceptor transparently retries any request that
  // races ahead of that renewal. Bouncing to /login here would otherwise
  // undo the whole point of having refresh tokens. If the refresh token
  // has also expired/been revoked, that same renewal attempt fails and
  // logs the user out, which flips isAuthenticated to false and this
  // route reactively redirects then.
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}
