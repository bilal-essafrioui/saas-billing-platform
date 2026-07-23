import { useState } from "react";
import { AxiosError } from "axios";

import { previewPlanChange } from "../api/subscription.api";
import type { ChangePlanRequest, ProrataResponse, ErrorResponse} from "../types/subscription.types";


export function usePreviewPlanChange() {
  const [loading, setLoading] = useState(false);
  const [preview, setPreview] = useState<ProrataResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchPreview = async (request: ChangePlanRequest) => {
    setLoading(true);
    setError(null);

    try {
      const response = await previewPlanChange(request);
      setPreview(response);
      return response;
    } catch (err) {
      const error = err as AxiosError<ErrorResponse>;
      setError(
        error.response?.data.message ??
          "Failed to preview plan change."
      );
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    preview,
    loading,
    error,
    fetchPreview,
  };
}