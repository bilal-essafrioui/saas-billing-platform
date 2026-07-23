import { useState } from "react";
import { AxiosError } from "axios";
import { changePlan } from "../api/subscription.api";
import type { ChangePlanRequest, SubscriptionResponse,} from "../types/subscription.types";

export function useChangePlan() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const confirmChangePlan = async (
    request: ChangePlanRequest
  ): Promise<SubscriptionResponse> => {
    setLoading(true);
    setError(null);

    try {
      return await changePlan(request);
    } catch (err) {
      const axiosError = err as AxiosError<{ message?: string }>;

      const message =
        axiosError.response?.data?.message ??
        "Failed to change subscription plan.";

      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    confirmChangePlan,
    loading,
    error,
  };
}