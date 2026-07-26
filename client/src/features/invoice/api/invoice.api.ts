import client from "../../../service/http/client";
import type { InvoiceResponse } from "../types/invoice.types";
import type { FilterInvoicesRequest } from "../types/filterInvoices.types";


export async function getMyInvoices(): Promise<InvoiceResponse[]> {
  const response = await client.get<InvoiceResponse[]>("/invoices/me");

  return response.data;
}

export async function filterInvoices(filters: FilterInvoicesRequest) {
  const response = await client.get<InvoiceResponse[]>(
    "/invoices/me/filter",
    {
      params: filters,
    }
  );

  return response.data;
}