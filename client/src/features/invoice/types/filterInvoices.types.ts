export type InvoiceStatusFilter = "PAID" | "PENDING" | "FAILED" | null;

export type PeriodValue =
  | "ALL_TIME"
  | "LAST_3_MONTHS"
  | "LAST_6_MONTHS"
  | "LAST_12_MONTHS";

export type FilterInvoicesRequest = {
  status: InvoiceStatusFilter;
  period: PeriodValue;
};