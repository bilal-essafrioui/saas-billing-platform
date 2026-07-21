import { useState } from "react";
import { AxiosError } from "axios";

import { getMyInvoices } from "../api/invoice.api";
import  type { InvoiceResponse, InvoiceErrorResponse } from "../types/invoice.types";

export function useMyInvoices() {
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<InvoiceErrorResponse | null>(null);

  const fetchInvoices = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getMyInvoices();

      setInvoices(data);
      console.log("Fetched invoices:", data);
    } catch (err) {
      const error = err as AxiosError<InvoiceErrorResponse>;

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

      setInvoices([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    invoices,
    loading,
    error,
    fetchInvoices,
  };
}