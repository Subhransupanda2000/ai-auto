import { apiClient } from './client';
import type { UserResponse } from '../types/auth';

export interface UpdateUserStatusRequest {
  enabled: boolean;
}

export interface UserPasswordReset {
  email: string;
  temporaryPassword: string;
}

/** Staff/team management within the caller's own clinic - ADMIN only (see
 * UserController). Creating a staff member is still `authApi.register`. */
export const userApi = {
  list: () => apiClient.get<UserResponse[]>('/users').then((res) => res.data),

  updateStatus: (id: string, payload: UpdateUserStatusRequest) =>
    apiClient.patch<UserResponse>(`/users/${id}/status`, payload).then((res) => res.data),

  resetPassword: (id: string) =>
    apiClient.post<UserPasswordReset>(`/users/${id}/reset-password`).then((res) => res.data),
};
