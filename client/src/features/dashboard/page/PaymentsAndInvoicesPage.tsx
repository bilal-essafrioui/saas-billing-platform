import { Pencil, X } from "lucide-react";
import Invoices from "../../invoice/component/Invoices";
import { usePaymentMethod } from "../../payment/hooks/usePaymentMethod";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import Spinner from "../../../components/ui/Spinner";
import { useSetupIntent } from "../../payment/hooks/useSetupIntent";
import { Elements } from "@stripe/react-stripe-js";
import { stripePromise } from "../../../lib/stripe";
import UpdateCardForm from "../../payment/component/UpdateCardForm";

/**
 * BillFlow "Payments & Invoices" page — main content only.
 */

export default function PaymentsAndInvoicesPage() {
  const { paymentMethod, loading, error, fetchPaymentMethod,} = usePaymentMethod();
  const [showUpdateCard, setShowUpdateCard] = useState(false);

  const { clientSecret, loading: setupLoading, error: setupError, loadSetupIntent,} = useSetupIntent();

  useEffect(() => {
    fetchPaymentMethod();
  }, []);

  useEffect(() => {
    if (setupError) {
      toast.error(setupError);
    }
  }, [setupError]);

  async function handleUpdateCard() {
    const secret = await loadSetupIntent();
    if (secret) {
      setShowUpdateCard(true);
    }
  }

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
            <span className="flex h-7 min-w-[44px] items-center justify-center rounded-[6px] bg-[var(--bg-card-hover)] px-2 text-[11px] font-bold italic tracking-wide text-[var(--text-primary)] whitespace-nowrap">
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
          onClick={handleUpdateCard}
          disabled={setupLoading}
          className="mt-3 flex w-full items-center justify-center gap-2 rounded-[var(--radius-button)] border border-[var(--border)] py-2.5 text-sm font-medium text-[var(--text-primary)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
        >
          {setupLoading ? (
            <>Preparing secure form...</>
          ) : (
            <>
              <Pencil className="h-3.5 w-3.5" />
              Update Card
            </>
          )}
          
        </button>
      </div>

      {/* Invoices */}
      <Invoices />

      {/* Stripe Payment Update Form — modal overlay */}
      {showUpdateCard && clientSecret && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-[var(--overlay)] backdrop-blur-sm px-4"
          onClick={() => setShowUpdateCard(false)}
        >
          <div
            className="w-full max-w-[440px] rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-modal)] p-6 shadow-[var(--shadow-card)]"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between">
              <h2 className="text-base font-semibold text-[var(--text-primary)]">Update Card</h2>
              <button
                type="button"
                onClick={() => setShowUpdateCard(false)}
                aria-label="Close"
                className="flex h-7 w-7 items-center justify-center rounded-full text-[var(--text-muted)] hover:bg-[var(--bg-card-hover)] hover:text-[var(--text-primary)] cursor-pointer"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <div className="mt-5">
              <Elements
                stripe={stripePromise}
                options={{
                  clientSecret,
                  locale: "en",
                  appearance: {
                    theme: "night",

                    variables: {
                      colorPrimary: "#5b5ef4",
                      colorBackground: "#1a1a24",
                      colorText: "#eeeef5",
                      colorTextPlaceholder: "#9898b0",
                      colorDanger: "#f87171",
                      borderRadius: "7px",
                      fontFamily: "Inter, sans-serif",
                      spacingUnit: "4px",
                    },

                    rules: {
                      ".Input": {
                        backgroundColor: "#13131a",
                        border: "1px solid #252535",
                        color: "#eeeef5",
                        boxShadow: "none",
                      },

                      ".Input:focus": {
                        border: "1px solid #5b5ef4",
                        boxShadow: "0 0 0 3px rgba(91,94,244,.2)",
                      },

                      ".Label": {
                        color: "#9898b0",
                      },

                      ".Tab": {
                        backgroundColor: "#13131a",
                        border: "1px solid #252535",
                        color: "#eeeef5",
                      },

                      ".Tab--selected": {
                        borderColor: "#5b5ef4",
                        backgroundColor: "#1f1f2e",
                      },

                      ".Block": {
                        backgroundColor: "#1a1a24",
                      },

                      ".Error": {
                        color: "#f87171",
                      },
                    },
                  },
                }}
              >
                <UpdateCardForm
                  onSuccess={async () => {

                    await fetchPaymentMethod();

                    toast.success("Payment method updated successfully.");

                    setShowUpdateCard(false);
                    
                  }}
                />
              </Elements>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}