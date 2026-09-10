export type Role = 'ADMIN' | 'DOCTOR' | 'RECEPTIONIST';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: Role;
}

export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

export interface AuthUser {
  email: string;
  role: Role;
  fullName?: string;
  /** The caller's own clinic name, decoded from the JWT - used to brand
   * the post-login UI (sidebar, etc.) per tenant instead of a static name. */
  tenantName?: string;
}

export interface MessageResponse {
  message: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
