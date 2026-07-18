import { useState } from "react";
import { checkout } from "../api/subscription.api";

export function useCheckout() {
  const [loading, setLoading] = useState(false);

  async function startCheckout(planId: string) {
    setLoading(true);

    try {
      return await checkout(planId);
    } finally {
      setLoading(false);
    }
  }

  return {
    startCheckout,
    loading,
  };
}