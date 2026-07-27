import { useState } from "react";
import { AxiosError } from "axios";

import { undoCancellation } from "../api/subscription.api";
import type {
  SubscriptionResponse,
  ErrorResponse,
} from "../types/subscription.types";

export function useUndoCancellation() {
  const [loading, setLoading] = useState(false);
  const [error, setError] =
    useState<AxiosError<ErrorResponse> | null>(null);

  const handleUndoCancellation  = async (): Promise<SubscriptionResponse> => {
    setLoading(true);
    setError(null);

    try {
      return await undoCancellation();
    } catch (err) {
      const axiosError =
        err as AxiosError<ErrorResponse>;

      setError(axiosError);
      throw axiosError;
    } finally {
      setLoading(false);
    }
  };

  return {
    undoCancellation: handleUndoCancellation,
    loading,
    error,
  };
}