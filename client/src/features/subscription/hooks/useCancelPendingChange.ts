import { useState } from "react";
import { AxiosError } from "axios";

import { cancelPendingChange } from "../api/subscription.api";
import type { SubscriptionResponse, ErrorResponse } from "../types/subscription.types";


export function useCancelPendingChange() {

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ErrorResponse | null>(null);


  const cancelChange = async (): Promise<SubscriptionResponse> => {

    try {
      setLoading(true);
      setError(null);

      return await cancelPendingChange();

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

      throw err;

    } finally {
      setLoading(false);
    }
  };


  return {
    cancelChange,
    loading,
    error,
  };
}