import { useState } from "react";
import { AlertTriangle } from "lucide-react";
import { useMySubscription } from "../../subscription/hooks/useMySubscription";
import SubscriptionEvents from "../../subscription/components/SubscriptionEvents";
import Spinner from "../../../components/ui/Spinner";
import { toast } from "sonner";
import Plans from "../../subscription/components/Plans";
import { usePreviewPlanChange } from "../../subscription/hooks/usePreviewPlanChange";
import { X } from "lucide-react";
import PlanChangePreviewModal from "../../subscription/components/PlanChangePreviewModal";
import { useChangePlan } from "../../subscription/hooks/useChangePlan";
import { useCancelPendingChange } from "../../subscription/hooks/useCancelPendingChange";
import { useCancelSubscription } from "../../subscription/hooks/useCancelSubscription";
import { useUndoCancellation } from "../../subscription/hooks/useUndoCancellation";

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

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function addDays(date: string, days: number) {
  const result = new Date(date);
  result.setDate(result.getDate() + days);

  return result.toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

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
  const [showPlans, setShowPlans] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [selectedPlanId, setSelectedPlanId] = useState<string | null>(null);
  const { subscription, loading, error, refetch } = useMySubscription();
  const { preview, loading: previewLoading, error: previewError, fetchPreview,} = usePreviewPlanChange();
  const { confirmChangePlan, loading: changeLoading, error: changeError,} = useChangePlan();
  const { cancelChange, loading: cancelLoading, error: cancelError,} = useCancelPendingChange();
  const { cancelSubscription, loading: cancellingSubscription, error: cancelSubscriptionError,} = useCancelSubscription();
  const { undoCancellation, loading: undoLoading, error: undoError,} = useUndoCancellation();
  

  if (loading) return <Spinner />;

  if (error) return toast.error(error.message);

  const startDate = subscription?.startDate
  ? new Date(subscription.startDate).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  : "N/A";

  const handlePreview = async (newPlanId: string) => {
    try {
      setSelectedPlanId(newPlanId);

      const response = await fetchPreview({ newPlanId });

      setShowPlans(false);
      setShowPreview(true);

    } catch (e) {
      console.error(e);
    }
  };

  const handleConfirmChange = async () => {
    if (!selectedPlanId) return;

    try {
      await confirmChangePlan({
        newPlanId: selectedPlanId,
      });

      await refetch();

      setShowPreview(false);
      setSelectedPlanId(null);

      toast.success("Subscription updated successfully.");
    } catch {
      // Le hook a déjà stocké l'erreur.
    }
  };

  const handleCancelPendingChange = async () => {

    try {

      await cancelChange();

      await refetch();

      toast.success(
        "Scheduled downgrade cancelled successfully."
      );

    } catch {
      if (cancelError) {
        toast.error(cancelError.message);
      }
    }
  };

  const handleCancelSubscription = async () => {
    try {
      await cancelSubscription();

      await refetch();

      toast.success("Subscription cancelled successfully.");
    } catch {
      if (cancelSubscriptionError) {
        toast.error(cancelSubscriptionError.message);
      }
    }
  };

  const handleUndoCancellation = async () => {
    try {
      await undoCancellation();

      await refetch();

      toast.success("Subscription cancellation undone successfully.");
    } catch {
      if (undoError) {
        toast.error(undoError.message);
      }
    }
  };

  const renewalDate = subscription?.nextRenewalDate
    ? new Date(subscription.nextRenewalDate).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    : "N/A";

  //
  const displayStatus: SubscriptionStatus =
  subscription?.status === "CANCELLED" &&
  subscription.nextRenewalDate &&
  new Date(subscription.nextRenewalDate) > new Date()
    ? "ACTIVE"
    : (subscription?.status as SubscriptionStatus);

  return (
    <div>
      <h1 className="text-[28px] font-semibold text-[var(--text-primary)]">My Subscription</h1>

      {/* Warning banner */}
      {subscription?.pendingPlanId && (
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
          <button
            type="button"
            onClick={handleCancelPendingChange}
            disabled={cancelLoading}
            className="ml-auto rounded-[var(--radius-button)] border border-[var(--warning-border)] px-3 py-1.5 text-xs font-medium text-[var(--warning-text)] hover:bg-[var(--warning-bg)] disabled:opacity-60 cursor-pointer"
          >
            {cancelLoading
              ? "Cancelling..."
              : "Cancel Scheduled Downgrade"}
          </button>
        </div>
      )}

      {/* PAST DUE BANNER*/}
      {/* Payment failed banner */}
      {subscription?.status === "PAST_DUE" && subscription.nextRenewalDate && (
        <div className="mt-5 flex items-start gap-3 rounded-[var(--radius-card)] border border-[var(--error-border)] bg-[var(--error-bg)] px-4 py-3">
          <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-[var(--error-text)]" />

          <div className="text-sm text-[var(--error-text)]">
            <p className="font-medium">
              Your subscription is currently past due.
            </p>

            <p className="mt-1">
              Your latest payment attempt failed. We will automatically retry the payment:
            </p>

            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>
                First retry: {addDays(subscription.nextRenewalDate, 1)}
              </li>
              <li>
                Second retry: {addDays(subscription.nextRenewalDate, 3)}
              </li>
              <li>
                Final retry: {addDays(subscription.nextRenewalDate, 7)}
              </li>
            </ul>

            <p className="mt-2">
              If all payment attempts fail, your subscription will be suspended.
            </p>
          </div>
        </div>
      )}

      {/* Cancelliation banner */}
      {subscription?.status === "CANCELLED" &&
      subscription.nextRenewalDate &&
      new Date(subscription.nextRenewalDate) > new Date() && (
        <div className="mt-5 flex items-center gap-3 rounded-[var(--radius-card)] border border-[var(--info-border)] bg-[var(--info-bg)] px-4 py-3">
          <AlertTriangle className="h-4 w-4 shrink-0 text-[var(--info-text)]" />

          <span className="text-sm text-[var(--info-text)]">
            Your subscription will end on{" "}
            <strong>
              {new Date(subscription.nextRenewalDate).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
                year: "numeric",
              })}
            </strong>
            . You can continue using your subscription until then.
          </span>

          <button
            type="button"
            onClick={handleUndoCancellation}
            disabled={undoLoading}
            className="ml-auto rounded-[var(--radius-button)] border border-[var(--info-border)] px-3 py-1.5 text-xs font-medium text-[var(--info-text)] hover:bg-[var(--info-border)] cursor-pointer"
          >
            {undoLoading ? "Undoing..." : "Undo Cancellation"}
          </button>
        </div>
    )}

      {/* Subscription card — full width */}
      <div className="mt-4 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-semibold text-[var(--text-primary)]">{subscription?.planName} Plan</h2>
            {subscription && (
              <Badge
                {...statusStyles[displayStatus]}
                label={displayStatus}
              />
            )}
            {/* 
            {subscription && (
              <Badge
                {...statusStyles[subscription.status]}
                label={subscription.status}
              />
            )}
            */}
            
          </div>

          <div className="flex items-center gap-2">
            { subscription?.status !== "CANCELLED" && (
              <button
                type="button"
                onClick={handleCancelSubscription}
                disabled={cancellingSubscription}
                className="rounded-[var(--radius-button)] border border-[var(--border)] px-4 py-2 text-sm font-medium text-[var(--error-text)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
              >
                {cancellingSubscription ? "Cancelling..." : "Cancel Subscription"}
              </button>
            )}
            <button
                type="button"
                onClick={() => setShowPlans(true)}
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

      {/* The modal */}
      {showPlans && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm"
          onClick={() => setShowPlans(false)}
        >
          <div
            className="relative max-h-[90vh] w-full max-w-5xl overflow-y-auto rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-page)] p-8 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              onClick={() => setShowPlans(false)}
              aria-label="Close"
              className="absolute right-6 top-6 rounded-full p-2 text-[var(--text-muted)] hover:bg-[var(--bg-card-hover)] hover:text-[var(--text-primary)] cursor-pointer"
            >
              <X className="h-5 w-5" />
            </button>

            <h2 className="text-2xl font-semibold text-[var(--text-primary)]">Change your plan</h2>
            <p className="mt-1 text-sm text-[var(--text-secondary)]">
              You're currently on the <strong>{subscription?.planName}</strong> plan.
              Choose a new plan below — the change will be reflected on your next billing cycle.
            </p>

            <Plans
              mode="change"
              currentPlanId={subscription?.planId}
              onPlanSelected={handlePreview}
            />
          </div>
        </div>
      )}
      
      {showPreview && preview && (
        <PlanChangePreviewModal
          preview={preview}
          loading={previewLoading}
          confirmLoading={changeLoading}
          onCancel={() => setShowPreview(false)}
          onConfirm={handleConfirmChange}
        />
      )}
    </div>
  );
}