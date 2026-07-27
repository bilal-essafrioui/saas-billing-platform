import { useState } from "react";
import { AxiosError } from "axios";

import { cancelSubscription } from "../api/subscription.api";
import type {
  SubscriptionResponse,
  ErrorResponse,
} from "../types/subscription.types";

export function useCancelSubscription() {
  const [loading, setLoading] = useState(false);
  const [error, setError] =
    useState<AxiosError<ErrorResponse> | null>(null);

  const handleCancelSubscription = async (): Promise<SubscriptionResponse> => {
    setLoading(true);
    setError(null);

    try {
      return await cancelSubscription();
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
    cancelSubscription: handleCancelSubscription,
    loading,
    error,
  };
}