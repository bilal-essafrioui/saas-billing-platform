import { AlertTriangle } from "lucide-react";
import { useMySubscription } from "../../subscription/hooks/useMySubscription";
import SubscriptionEvents from "../../subscription/components/SubscriptionEvents";
import Spinner from "../../../components/ui/Spinner";
import { toast } from "sonner";
/**
 * BillFlow "My Subscription" page — main content only.
 * Payment method card intentionally omitted (as requested), so the
 * subscription card takes the full width but keeps the same height.
 *
 * The events table below is populated with static mock data matching
 * the SubscriptionEvent shape from the backend.
 */

type SubscriptionStatus = "ACTIVE" | "PAST_DUE" | "SUSPENDED" | "CANCELLED";

// Adjust this union to match your actual SubscriptionEventType enum values.


const statusStyles: Record<SubscriptionStatus, { bg: string; text: string; border: string }> = {
  ACTIVE: { bg: "var(--success-bg)", text: "var(--success-text)", border: "var(--success-border)" },
  PAST_DUE: { bg: "var(--warning-bg)", text: "var(--warning-text)", border: "var(--warning-border)" },
  SUSPENDED: { bg: "var(--error-bg)", text: "var(--error-text)", border: "var(--error-border)" },
  CANCELLED: { bg: "var(--bg-card-hover)", text: "var(--text-muted)", border: "var(--border)" },
};



function Badge({ bg, text, border, label }: { bg: string; text: string; border: string; label: string }) {
  return (
    <span
      className="rounded-[var(--radius-badge)] border px-2 py-0.5 text-[11px] font-semibold tracking-wide"
      style={{ backgroundColor: bg, color: text, borderColor: border }}
    >
      {label}
    </span>
  );
}


export default function MySubscriptionPage() {
  const { subscription, loading, error } = useMySubscription();

  if (loading) return <Spinner />;

  if (error) return toast.error(error.message);

  const startDate = subscription?.startDate
  ? new Date(subscription.startDate).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  : "N/A";

const renewalDate = subscription?.nextRenewalDate
  ? new Date(subscription.nextRenewalDate).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  : "N/A";

  return (
    <div>
      <h1 className="text-[28px] font-semibold text-[var(--text-primary)]">My Subscription</h1>

      {/* Warning banner */}
      {subscription?.pendingPlanName && (
        <div className="mt-5 flex items-center gap-3 rounded-[var(--radius-card)] border border-[var(--warning-border)] bg-transparent px-4 py-3">
          <AlertTriangle className="h-4 w-4 shrink-0 text-[var(--warning-text)]" />

          <span className="text-sm text-[var(--warning-text)]">
            Your plan will change to{" "}
            <strong>{subscription.pendingPlanName}</strong>{" "}
            on{" "}
            {new Date(subscription.pendingPlanEffectiveDate!).toLocaleDateString(
              "en-US",
              {
                month: "short",
                day: "numeric",
                year: "numeric",
              }
            )}
          </span>
        </div>
      )}

      {/* Subscription card — full width */}
      <div className="mt-4 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-semibold text-[var(--text-primary)]">{subscription?.planName} Plan</h2>
            {subscription && (
              <Badge
                {...statusStyles[subscription.status]}
                label={subscription.status}
              />
            )}
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              className="rounded-[var(--radius-button)] border border-[var(--border)] px-4 py-2 text-sm font-medium text-[var(--error-text)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
            >
              Cancel Subscription
            </button>
            <button
              type="button"
              className="rounded-[var(--radius-button)] bg-[var(--primary)] px-4 py-2 text-sm font-medium text-white hover:bg-[var(--primary-hover)] cursor-pointer"
            >
              Change Plan
            </button>
          </div>
        </div>

        <p className="mt-2 text-2xl font-semibold text-[var(--text-primary)]">
          ${subscription?.planPrice.toFixed(2)}
          <span className="ml-1 text-sm font-normal text-[var(--text-secondary)]">/ month</span>
        </p>

        <div className="mt-5 grid grid-cols-4 gap-4 border-t border-[var(--border)] pt-4">
          <div>
            <p className="text-xs tracking-wide text-[var(--text-muted)]">START DATE</p>
            <p className="mt-1 text-sm text-[var(--text-primary)]">{startDate}</p>
          </div>
          <div>
            <p className="text-xs tracking-wide text-[var(--text-muted)]">RENEWAL DATE</p>
            <p className="mt-1 text-sm text-[var(--text-primary)]">{renewalDate}</p>
          </div>
          <div>
            <p className="text-xs tracking-wide text-[var(--text-muted)]">AMOUNT</p>
            <p className="mt-1 text-sm text-[var(--text-primary)]">${subscription?.planPrice.toFixed(2)} USD</p>
          </div>
          <div>
            <p className="text-xs tracking-wide text-[var(--text-muted)]">BILLING TYPE</p>
            <p className="mt-1 text-sm text-[var(--text-primary)]">Monthly</p>
          </div>
        </div>
      </div>

      {/* Subscription events history */}
      <SubscriptionEvents />
      
    </div>
  );
}