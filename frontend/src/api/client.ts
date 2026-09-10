import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '../types/common';
import { useAuthStore } from '../store/authStore';
import { queryClient } from '../lib/queryClient';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

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
  (error: AxiosError<{ message?: string; error?: string; details?: string[] }>) => {
    if (error.response?.status === 401) {
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
