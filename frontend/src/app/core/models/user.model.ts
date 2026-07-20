export type UserRole = 'RIDER' | 'DRIVER' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  fullName: string;
  phoneNumber: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string;
  role: UserRole;
}
