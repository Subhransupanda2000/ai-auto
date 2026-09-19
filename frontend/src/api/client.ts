import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '../types/common';
import { useAuthStore } from '../store/authStore';
import { queryClient } from '../lib/queryClient';
import { API_BASE_URL } from './env';
import { refreshAccessToken } from './refreshAuth';

export { API_BASE_URL };

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retriedAfterRefresh?: boolean;
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<{ message?: string; error?: string; details?: string[] }>) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    const isAuthEndpoint = originalRequest?.url?.includes('/auth/');

    // A 401 on any other request usually just means the access token
    // expired between AuthWatcher's proactive refresh checks - try once to
    // silently renew it and replay the original request before giving up
    // and signing the user out.
    if (error.response?.status === 401 && originalRequest && !originalRequest._retriedAfterRefresh && !isAuthEndpoint) {
      originalRequest._retriedAfterRefresh = true;
      const newAccessToken = await refreshAccessToken();
      if (newAccessToken) {
        originalRequest.headers.set('Authorization', `Bearer ${newAccessToken}`);
        return apiClient(originalRequest);
      }

      useAuthStore.getState().logout();
      // Prevents stale cross-session/cross-tenant data (patients, doctors,
      // appointments, ...) from lingering and being shown to whoever logs
      // in next in this browser tab.
      queryClient.clear();
    }

    const apiError: ApiError = {
      status: error.response?.status ?? 0,
      message:
        error.response?.data?.message ??
        error.response?.data?.error ??
        error.message ??
        'An unexpected error occurred.',
      details: error.response?.data?.details ?? null,
    };
    return Promise.reject(apiError);
  },
);
