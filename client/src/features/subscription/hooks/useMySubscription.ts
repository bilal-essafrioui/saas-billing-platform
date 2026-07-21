import { useEffect, useState } from "react";
import { AxiosError } from "axios";

import { getMySubscription } from "../api/subscription.api";
import type { SubscriptionResponse, ErrorResponse} from "../types/subscription.types";

export function useMySubscription() {
  const [subscription, setSubscription] =
    useState<SubscriptionResponse | null>(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<ErrorResponse | null>(null);

  const fetchSubscription = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getMySubscription();
      setSubscription(data);
    } catch (err) {
      const error = err as AxiosError<ErrorResponse>;

      if (error.response) {
        setError(error.response.data);
      } else {
        setError({
          status: 500,
          error: "UNKNOWN_ERROR",
          message: "Something went wrong.",
          timestamp: new Date().toISOString(),
        });
      }

      setSubscription(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubscription();
  }, []);

  return {
    subscription,
    loading,
    error,
    refetch: fetchSubscription,
  };
}