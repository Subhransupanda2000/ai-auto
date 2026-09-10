import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '../types/common';
import { useSuperAdminAuthStore } from '../store/superAdminAuthStore';
import { API_BASE_URL } from './client';
import { queryClient } from '../lib/queryClient';

/**
 * Dedicated axios instance for the super-admin platform surface: it reads
 * its bearer token from {@code useSuperAdminAuthStore}, never from the
 * staff {@code useAuthStore}, so the two login sessions can never leak
 * into each other's requests.
 */
export const superAdminClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

superAdminClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useSuperAdminAuthStore.getState().accessToken;
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

superAdminClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message?: string; error?: string; details?: string[] }>) => {
    if (error.response?.status === 401) {
      useSuperAdminAuthStore.getState().logout();
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
