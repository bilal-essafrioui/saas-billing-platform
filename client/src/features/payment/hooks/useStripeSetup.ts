import { useState } from "react";
import type { FormEvent } from "react";
import { useStripe, useElements } from "@stripe/react-stripe-js";

export function useStripeSetup() {
  const stripe = useStripe();
  const elements = useElements();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function confirmSetup(e: FormEvent): Promise<boolean> {
    e.preventDefault();

    if (!stripe || !elements) {
      return false;
    }

    setLoading(true);
    setError("");

    try {
      const result = await stripe.confirmSetup({
        elements,
        redirect: "if_required",
      });

      console.log(result);

      const { error } = result;

      if (error) {
        setError(error.message ?? "Unable to update payment method.");
        return false;
      }

      return true;
    } finally {
      setLoading(false);
    }
  }

  return {
    stripe,
    loading,
    error,
    confirmSetup,
  };
}