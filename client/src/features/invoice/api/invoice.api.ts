import client from "../../../service/http/client";
import type { InvoiceResponse } from "../types/invoice.types";

export async function getMyInvoices(): Promise<InvoiceResponse[]> {
  const response = await client.get<InvoiceResponse[]>("/invoices/me");

  return response.data;
}