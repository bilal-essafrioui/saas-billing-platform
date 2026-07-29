import { useState } from "react";
import { createSetupIntent } from "../api/payment.api";

export function useSetupIntent() {
  const [loading, setLoading] = useState(false);
  const [clientSecret, setClientSecret] = useState("");
  const [error, setError] = useState("");

  async function loadSetupIntent() {
    try {
      setLoading(true);
      setError("");

      const response = await createSetupIntent();

      setClientSecret(response.clientSecret);

      return response.clientSecret;

    } catch (err: any) {
      setError(
        err?.response?.data?.message ??
        "Unable to initialize card update."
      );
    } finally {
      setLoading(false);
    }
  }

  return {
    loading,
    error,
    clientSecret,
    loadSetupIntent,
  };
}