import { ArrowRight } from "lucide-react";
import { useMyInvoices } from "../hooks/useMyInvoices";
import { useEffect } from "react";


export type InvoiceStatus = "PENDING" | "PAID" | "FAILED";

export type Invoice = {
  id: string;
  date: string; // e.g. "Jul 2026"
  amount: string; // e.g. "€30.00"
  status: InvoiceStatus;
};


const invoiceStatusConfig: Record<InvoiceStatus, { label: string; bg: string; text: string; border: string }> = {
  PENDING: {
    label: "PENDING",
    bg: "var(--warning-bg)",
    text: "var(--warning-text)",
    border: "var(--warning-border)",
  },
  PAID: {
    label: "PAID",
    bg: "var(--success-bg)",
    text: "var(--success-text)",
    border: "var(--success-border)",
  },
  FAILED: {
    label: "FAILED",
    bg: "var(--error-bg)",
    text: "var(--error-text)",
    border: "var(--error-border)",
  },
};

function StatusBadge({ label, bg, text, border }: { label: string; bg: string; text: string; border: string }) {
  return (
    <span
      className="rounded-[var(--radius-badge)] border px-2 py-0.5 text-[11px] font-semibold tracking-wide"
      style={{ backgroundColor: bg, color: text, borderColor: border }}
    >
      {label}
    </span>
  );
}


export default function RecentInvoices() {
    const { invoices, loading, error, fetchInvoices } = useMyInvoices();

    useEffect(() => {
        fetchInvoices();
    }, []);

    const displayedInvoices: Invoice[] = invoices.map((invoice) => ({
        id: invoice.id,

        date: new Date(invoice.billingPeriodStart).toLocaleDateString("en-US", {
            month: "short",
            year: "numeric",
        }),

        amount: `$${invoice.amount.toFixed(2)}`,

        status: invoice.status,
    }));
    return (
        <div className="rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-semibold text-[var(--text-primary)]">Recent Invoices</h2>
            <button
              type="button"
              className="flex items-center gap-1 text-sm text-[var(--text-link)] hover:underline"
            >
              View all
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>

          <table className="mt-4 w-full text-sm">
            <thead>
              <tr className="text-left text-xs text-[var(--text-muted)]">
                <th className="pb-2 font-normal">Invoice Date</th>
                <th className="pb-2 font-normal">Amount</th>
                <th className="pb-2 text-right font-normal">Status</th>
              </tr>
            </thead>
            <tbody>
              {displayedInvoices.map((invoice) => {
                const config = invoiceStatusConfig[invoice.status];
                return (
                  <tr key={invoice.id} className="border-t border-[var(--border)]">
                    <td className="py-3 font-medium text-[var(--text-primary)]">{invoice.date}</td>
                    <td className="py-3 text-[var(--text-secondary)]">{invoice.amount}</td>
                    <td className="py-3 text-right">
                      <StatusBadge
                        label={config.label}
                        bg={config.bg}
                        text={config.text}
                        border={config.border}
                      />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

    );
}