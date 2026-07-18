import { useState } from "react";
import type { FormEvent } from "react";
import { useStripe, useElements } from "@stripe/react-stripe-js";

export function useStripeCheckout() {
  const stripe = useStripe();
  const elements = useElements();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function confirmPayment(e: FormEvent) {
    e.preventDefault();

    if (!stripe || !elements) return;

    setLoading(true);
    setError("");

    const { error } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/payment/success`,
      },
    });

    if (error) {
      setError(error.message ?? "Payment failed.");
      setLoading(false);
    }
  }

  return {
    loading,
    error,
    confirmPayment,
    stripe,
  };
}