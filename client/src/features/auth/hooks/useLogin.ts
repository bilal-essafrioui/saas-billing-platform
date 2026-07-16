import { useState } from "react";
import { login } from "../api/auth.api";
import type {
  LoginRequest,
  LoginResponse,
  ApiError,
} from "../types/auth.types";

export function useLogin() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function signIn(
    request: LoginRequest
  ): Promise<LoginResponse | null> {
    setLoading(true);
    setError(null);

    try {
      const response = await login(request);

      return response;
    } catch (err: any) {
      const apiError: ApiError = err.response?.data;

      setError(
        apiError?.message ??
          "Something went wrong."
      );

      return null;
    } finally {
      setLoading(false);
    }
  }

  return {
    signIn,
    loading,
    error,
  };
}