import type { ReactElement } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSuperAdminAuthStore } from '../store/superAdminAuthStore';

interface SuperAdminProtectedRouteProps {
  children: ReactElement;
}

export function SuperAdminProtectedRoute({ children }: SuperAdminProtectedRouteProps) {
  const isAuthenticated = useSuperAdminAuthStore((s) => s.isAuthenticated);
  const isTokenExpired = useSuperAdminAuthStore((s) => s.isTokenExpired);
  const location = useLocation();

  if (!isAuthenticated || isTokenExpired()) {
    return <Navigate to="/super-admin/login" replace state={{ from: location }} />;
  }

  return children;
}
