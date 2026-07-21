// register types
export type RegisterRequest = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
};

export type RegisterResponse = {
  message: string;
  email: string;
};

export type RegisterApiError = {
  status: number;
  error: string;
  message: string;
  timestamp: string;
};

// login types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: "CUSTOMER" | "ADMIN";
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

// Email Verification types
export interface VerifyEmailRequest {
  email: string;
  otp: string;
}

export interface VerifyEmailResponse {
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: "CUSTOMER" | "ADMIN";
}

export interface VerifyEmailApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: "CUSTOMER" | "ADMIN";
  lastLoginAt: string;
  createdAt: string;
}