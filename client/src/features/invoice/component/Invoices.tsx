import { FileText, AlertCircle, ChevronDown, Download, RotateCw, Eye } from "lucide-react";
import type { InvoiceStatus, InvoicesType, InvoiceType } from "../types/invoice.types";
import { useMyInvoices } from "../hooks/useMyInvoices";
import { toast } from "sonner";
import { useEffect } from "react";

const statusStyles: Record<InvoiceStatus, { text: string }> = {
  PENDING: { text: "var(--warning-text)" },
  PAID: { text: "var(--success-text)" },
  FAILED: { text: "var(--error-text)" },
};

const typeStyles: Record<InvoicesType, { bg: string; text: string; border: string }> = {
  RECURRING: { bg: "var(--bg-card-hover)", text: "var(--text-secondary)", border: "var(--border)" },
  PRORATION: { bg: "var(--warning-bg)", text: "var(--warning-text)", border: "var(--warning-border)" },
};

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

function TypeBadge({ type }: { type: InvoicesType }) {
  const style = typeStyles[type];
  return (
    <span
      className="rounded-[var(--radius-badge)] border px-2.5 py-0.5 text-xs font-medium"
      style={{ backgroundColor: style.bg, color: style.text, borderColor: style.border }}
    >
      {type}
    </span>
  );
}

export default function Invoices(){
    const { invoices, loading, error, fetchInvoices } = useMyInvoices();
    const isInvoiceType = (
        type: InvoiceType | InvoicesType
        ): type is InvoicesType =>
        type === "RECURRING" || type === "PRORATION";

    useEffect(() => {
        fetchInvoices();
    }, []);
    if (loading) return;

    if (error) return toast.error(error.message);



    return (
    
      <div className="mt-4 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-base font-semibold text-[var(--text-primary)]">Invoices</h2>

          <div className="flex items-center gap-2">
            <div className="relative">
              <select
                className="appearance-none rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-2 pl-3 pr-8 text-sm text-[var(--text-primary)] outline-none focus:border-[var(--border-active)]"
              >
                <option>All statuses</option>
                <option>Pending</option>
                <option>Paid</option>
                <option>Failed</option>
              </select>
              <ChevronDown className="pointer-events-none absolute right-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--text-muted)]" />
            </div>

            <div className="relative">
              <select
                className="appearance-none rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-2 pl-3 pr-8 text-sm text-[var(--text-primary)] outline-none focus:border-[var(--border-active)]"
              >
                <option>July 2026</option>
                <option>June 2026</option>
                <option>May 2026</option>
                <option>April 2026</option>
              </select>
              <ChevronDown className="pointer-events-none absolute right-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--text-muted)]" />
            </div>
          </div>
        </div>

        <div className="mt-4 overflow-x-auto">
          <table className="w-full min-w-[700px] border-collapse text-sm">
            <thead>
              <tr className="border-b border-[var(--border)] text-left text-[11px] uppercase tracking-wide text-[var(--text-muted)]">
                <th className="pb-3 pr-4 font-medium">Invoice / Period</th>
                <th className="pb-3 pr-4 font-medium">Type</th>
                <th className="pb-3 pr-4 font-medium">Amount</th>
                <th className="pb-3 font-medium">Status</th>
                <th className="pb-3 font-medium">Paid At</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map((invoice) => {
                const isFailed = invoice.status === "FAILED";

                return (
                  <tr
                    key={invoice.id}
                    className="border-b border-[var(--border)] last:border-0 hover:bg-[var(--bg-card-hover)]"
                  >
                    <td className="py-4 pr-4 align-top">
                      <div className="flex items-start gap-3">
                        {isFailed ? (
                          <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-[var(--radius-input)] bg-[var(--error-bg)]">
                            <AlertCircle className="h-4 w-4 text-[var(--error-text)]" />
                          </span>
                        ) : (
                          <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-[var(--radius-input)] bg-[var(--bg-card-hover)]">
                            <FileText className="h-4 w-4 text-[var(--text-muted)]" />
                          </span>
                        )}
                        <div>
                          <p className="font-medium text-[var(--text-primary)]">{invoice.idempotencyKey}</p>
                          <p className="text-xs text-[var(--text-muted)]">{formatDate(invoice.billingPeriodStart)} - {formatDate(invoice.billingPeriodEnd)}</p>
                        </div>
                      </div>
                    </td>

                    <td className="py-4 pr-4 align-top">
                        {isInvoiceType(invoice.type) && (
                        <TypeBadge type={invoice.type} />
                        )}
                    </td>

                    <td className="py-4 pr-4 align-top whitespace-nowrap font-medium text-[var(--text-primary)]">
                      ${invoice.amount}
                    </td>

                    <td className="py-4 align-top">
                      <span className="text-sm font-medium" style={{ color: statusStyles[invoice.status].text }}>
                        {invoice.status === "PAID" ? "Paid" : invoice.status === "FAILED" ? "Failed" : "Pending"}
                      </span>
                    </td>

                    <td className="py-4 align-top">
                      <span className="text-sm font-medium">
                        {invoice.paidAt != null ? formatDate(invoice.paidAt) : "_"}
                      </span>
                    </td>



                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
}