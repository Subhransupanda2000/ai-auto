import { apiClient } from './client';
import type { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../types/auth';

export const authApi = {
  login: (payload: LoginRequest) =>
    apiClient.post<LoginResponse>('/auth/login', payload).then((res) => res.data),

  register: (payload: RegisterRequest) =>
    apiClient.post<UserResponse>('/auth/register', payload).then((res) => res.data),
};
