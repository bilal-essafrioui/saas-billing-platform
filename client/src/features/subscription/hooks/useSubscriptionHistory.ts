import { useState } from "react";
import { AxiosError } from "axios";

import { getMySubscriptionHistory } from "../api/subscription.api";

import type { SubscriptionEventResponse, SubscriptionHistoryErrorResponse } from "../types/subscription.types";

export function useSubscriptionHistory() {
  const [events, setEvents] = useState<SubscriptionEventResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] =
    useState<SubscriptionHistoryErrorResponse | null>(null);

  const fetchSubscriptionHistory = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getMySubscriptionHistory();
      console.log(data);

      setEvents(data);
    } catch (err) {
      const error =
        err as AxiosError<SubscriptionHistoryErrorResponse>;

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

      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    events,
    loading,
    error,
    fetchSubscriptionHistory,
  };
}