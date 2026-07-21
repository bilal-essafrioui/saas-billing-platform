import { Pencil } from "lucide-react";
import Invoices from "../../invoice/component/Invoices";
import { usePaymentMethod } from "../../payment/hooks/usePaymentMethod";
import { useEffect } from "react";
import { toast } from "sonner";
import Spinner from "../../../components/ui/Spinner";
/**
 * BillFlow "Payments & Invoices" page — main content only.
 */







export default function PaymentsAndInvoicesPage() {
  const { paymentMethod, loading, error, fetchPaymentMethod,} = usePaymentMethod();

  useEffect(() => {
    fetchPaymentMethod();
  }, []);

  if (loading) return <Spinner />;

  if (error) toast.error(error.message);

  return (
    <div>
      <h1 className="text-[28px] font-semibold text-[var(--text-primary)]">Payments & Invoices</h1>

      {/* Payment method — full width */}
      <div className="mt-6 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
        <h2 className="text-base font-semibold text-[var(--text-primary)]">Payment Method</h2>

        <div className="mt-4 flex items-center justify-between rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] px-4 py-3.5">
          <div className="flex items-center gap-3">
            <span className="flex h-7 w-11 items-center justify-center rounded-[6px] bg-[var(--bg-card-hover)] text-[11px] font-bold italic tracking-wide text-[var(--text-primary)]">
              {paymentMethod?.brand}
            </span>
            <div>
              <p className="text-sm font-medium text-[var(--text-primary)]">•••• {paymentMethod?.last4}</p>
              <p className="text-xs text-[var(--text-secondary)]">Expires {paymentMethod?.expMonth}/{paymentMethod?.expYear}</p>
            </div>
          </div>

          <span className="flex h-6 w-6 items-center justify-center rounded-full border border-[var(--success-border)] bg-[var(--success-bg)]">
            <svg viewBox="0 0 24 24" className="h-3.5 w-3.5" fill="none" stroke="var(--success-text)" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
        </div>

        <button
          type="button"
          className="mt-3 flex w-full items-center justify-center gap-2 rounded-[var(--radius-button)] border border-[var(--border)] py-2.5 text-sm font-medium text-[var(--text-primary)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
        >
          <Pencil className="h-3.5 w-3.5" />
          Update Card
        </button>
      </div>

      {/* Invoices */}
      <Invoices />
    </div>
  );
}