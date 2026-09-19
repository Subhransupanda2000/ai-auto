import { apiClient } from './client';
import type {
  ChangePasswordRequest,
  ForgotPasswordRequest,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  RefreshTokenRequest,
  RegisterRequest,
  ResetPasswordRequest,
  UserResponse,
} from '../types/auth';

export const authApi = {
  login: (payload: LoginRequest) =>
    apiClient.post<LoginResponse>('/auth/login', payload).then((res) => res.data),

  refresh: (payload: RefreshTokenRequest) =>
    apiClient.post<LoginResponse>('/auth/refresh', payload).then((res) => res.data),

  logout: (payload: RefreshTokenRequest) =>
    apiClient.post<MessageResponse>('/auth/logout', payload).then((res) => res.data),

  register: (payload: RegisterRequest) =>
    apiClient.post<UserResponse>('/auth/register', payload).then((res) => res.data),

  forgotPassword: (payload: ForgotPasswordRequest) =>
    apiClient.post<MessageResponse>('/auth/forgot-password', payload).then((res) => res.data),

  resetPassword: (payload: ResetPasswordRequest) =>
    apiClient.post<MessageResponse>('/auth/reset-password', payload).then((res) => res.data),

  changePassword: (payload: ChangePasswordRequest) =>
    apiClient.post<MessageResponse>('/auth/change-password', payload).then((res) => res.data),
};
