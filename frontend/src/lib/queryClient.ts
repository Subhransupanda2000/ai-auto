import { QueryClient } from '@tanstack/react-query';

/**
 * Single shared QueryClient instance, exported (not created inline in
 * App.tsx) so that anything logging a session out - the axios 401
 * interceptor, useAuth/useSuperAdminAuth, AuthWatcher's auto-logout on
 * token expiry - can call {@code queryClient.clear()}. Without this,
 * cached data (patients, doctors, appointments, ...) from a previous
 * session/tenant would keep showing up after logging in as a different
 * user, since none of the query keys are tenant-scoped.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
