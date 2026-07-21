export type InvoiceStatus = "PENDING" | "PAID" | "FAILED";

export type InvoiceType =
  | "INITIAL_SUBSCRIPTION"
  | "RENEWAL"
  | "UPGRADE"
  | "DOWNGRADE";

export type InvoicesType =
  | "RECURRING"
  | "PRORATION";

export interface InvoiceResponse {
  id: string;
  subscriptionId: string;
  userId: string;
  userEmail: string;

  amount: number;

  status: InvoiceStatus;
  type: InvoiceType | InvoicesType;

  idempotencyKey: string;

  billingPeriodStart: string;
  billingPeriodEnd: string;

  paidAt: string | null;
  createdAt: string;
}

export interface InvoiceErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}