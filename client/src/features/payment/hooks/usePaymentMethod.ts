import { useCallback, useState } from "react";
import { AxiosError } from "axios";
import { getMyPaymentMethod } from "../api/payment.api";
import type { PaymentMethod } from "../types/payment.types";

export const usePaymentMethod = () => {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<AxiosError | null>(null);

  const fetchPaymentMethod = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getMyPaymentMethod();
      console.log("API returned:", data);

      setPaymentMethod(data);
    } catch (err) {
      setError(err as AxiosError);
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    paymentMethod,
    loading,
    error,
    fetchPaymentMethod,
  };
};