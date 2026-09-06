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
}
