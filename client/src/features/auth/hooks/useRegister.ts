import { useState } from "react";
import { AxiosError } from "axios";
import { register } from "../api/auth.api";
import type {
  RegisterRequest,
  RegisterResponse,
  RegisterApiError,
} from "../types/auth.types";

export function useRegister() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (
    request: RegisterRequest
  ): Promise<RegisterResponse | null> => {
    try {
      setLoading(true);
      setError("");

      return await register(request);

    } catch (err) {

      const error = err as AxiosError<RegisterApiError>;

      console.log(error.response?.data);

      setError(
        error.response?.data?.message ??
        "Unable to register."
      );

      return null;

    } finally {
      setLoading(false);
    }
  };

  return {
    submit,
    loading,
    error,
  };
}