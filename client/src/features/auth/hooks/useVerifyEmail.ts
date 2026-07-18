import { useState } from "react";
import { AxiosError } from "axios";

import { verifyEmail } from "../api/auth.api";
import type {
  VerifyEmailRequest,
  VerifyEmailResponse,
  VerifyEmailApiError,
} from "../types/auth.types";

export function useVerifyEmail() {

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (
    request: VerifyEmailRequest
  ): Promise<VerifyEmailResponse | null> => {

    try {
      setLoading(true);
      setError("");

      return await verifyEmail(request);

    } catch (err) {

      const error = err as AxiosError<VerifyEmailApiError>;

      setError(
        error.response?.data?.message ??
        "Unable to verify email."
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