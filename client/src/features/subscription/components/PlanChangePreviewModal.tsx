import { ArrowDown, X } from "lucide-react";
import type { ProrataResponse } from "../types/subscription.types";

type PlanChangePreviewModalProps = {
  preview: ProrataResponse;
  loading?: boolean;
  confirmLoading?: boolean;
  onCancel: () => void;
  onConfirm: () => void;
};

function formatDate(dateStr: string | null) {
  if (!dateStr) return "N/A";
  return new Date(dateStr).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function PlanRow({
  label,
  planName,
  price,
  emphasis,
}: {
  label: string;
  planName: string;
  price: number;
  emphasis?: boolean;
}) {
  return (
    <div className="flex items-center justify-between py-3">
      <span className="text-xs font-medium tracking-wide text-[var(--text-muted)]">
        {label}
      </span>
      <div className="flex items-baseline gap-2">
        <span
          className={`text-sm font-semibold ${
            emphasis ? "text-[var(--text-primary)]" : "text-[var(--text-secondary)]"
          }`}
        >
          {planName}
        </span>
        <span className="text-sm text-[var(--text-secondary)]">
          ${price}
          <span className="text-xs">/mo</span>
        </span>
      </div>
    </div>
  );
}

export default function PlanChangePreviewModal({
  preview,
  loading,
  confirmLoading,
  onCancel,
  onConfirm,
}: PlanChangePreviewModalProps) {
  const isDowngrade = preview.changeType === "DOWNGRADE";
  const effectiveDate = formatDate(preview.effectiveDate);
  const isLoading = loading || confirmLoading;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm"
      onClick={onCancel}
    >
      <div
        className="relative w-full max-w-md rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-modal)] p-7 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          onClick={onCancel}
          aria-label="Close"
          className="absolute right-5 top-5 rounded-full p-1.5 text-[var(--text-muted)] hover:bg-[var(--bg-card-hover)] hover:text-[var(--text-primary)] cursor-pointer"
        >
          <X className="h-4 w-4" />
        </button>

        {/* Header */}
        <h2 className="text-lg font-semibold text-[var(--text-primary)]">
          {isDowngrade ? "Confirm Plan Downgrade" : "Confirm Plan Upgrade"}
        </h2>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">
          {isDowngrade
            ? "Review the details before scheduling your downgrade."
            : "You're changing your subscription. Review the charge below."}
        </p>

        {/* Plan comparison */}
        <div className="mt-5 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] px-4">
          <PlanRow
            label="CURRENT PLAN"
            planName={preview.currentPlanName}
            price={preview.currentPlanPrice}
          />

          <div className="flex justify-center border-y border-[var(--border)] py-1.5">
            <div className="flex h-6 w-6 items-center justify-center rounded-full bg-[var(--bg-card-hover)]">
              <ArrowDown className="h-3.5 w-3.5 text-[var(--text-muted)]" />
            </div>
          </div>

          <PlanRow
            label="NEW PLAN"
            planName={preview.newPlanName}
            price={preview.newPlanPrice}
            emphasis
          />
        </div>

        {/* Payment / schedule details */}
        <div className="mt-5 rounded-[var(--radius-card)] border border-[var(--border)] px-4 py-4">
          {isDowngrade ? (
            <>
              <div className="flex items-center justify-between">
                <span className="text-sm text-[var(--text-secondary)]">
                  Payment required today
                </span>
                <span className="text-sm font-semibold text-[var(--success-text)]">
                  $0.00
                </span>
              </div>
              <p className="mt-3 text-sm leading-relaxed text-[var(--text-secondary)]">
                Your current <strong className="text-[var(--text-primary)]">{preview.currentPlanName}</strong> subscription
                remains active until{" "}
                <strong className="text-[var(--text-primary)]">{effectiveDate}</strong>.
              </p>
              <div className="mt-4 rounded-[var(--radius-button)] border border-[var(--warning-border)] bg-[var(--warning-bg)] px-3 py-2.5">
                <p className="text-xs leading-relaxed text-[var(--warning-text)]">
                  On {effectiveDate}, your subscription will automatically change to{" "}
                  <strong>{preview.newPlanName} (${preview.newPlanPrice}/mo)</strong>.
                </p>
              </div>
            </>
          ) : (
            <>
              <div className="flex items-center justify-between">
                <span className="text-sm text-[var(--text-secondary)]">
                  Amount due today
                </span>
                <span className="text-lg font-semibold text-[var(--text-primary)]">
                  ${preview.amountToPayNow?.toFixed(2)}
                </span>
              </div>
              <p className="mt-2 text-xs leading-relaxed text-[var(--text-muted)]">
                This charge is prorated for the remaining {preview.remainingDays} days
                of your {preview.totalDays}-day billing cycle.
              </p>
              <div className="mt-4 rounded-[var(--radius-button)] border border-[var(--border)] bg-[var(--bg-card-hover)] px-3 py-2.5">
                <p className="text-xs leading-relaxed text-[var(--text-secondary)]">
                  Starting on <strong className="text-[var(--text-primary)]">{effectiveDate}</strong>, your subscription will
                  renew at{" "}
                  <strong className="text-[var(--text-primary)]">${preview.newPlanPrice}/month</strong>.
                </p>
              </div>
            </>
          )}
        </div>

        {/* Footer actions */}
        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="rounded-[var(--radius-button)] border border-[var(--border)] px-4 py-2.5 text-sm font-medium text-[var(--text-primary)] hover:bg-[var(--bg-card-hover)] cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isLoading}
            className={`rounded-[var(--radius-button)] px-4 py-2.5 text-sm font-medium text-white cursor-pointer disabled:cursor-not-allowed disabled:opacity-60 ${
              isDowngrade
                ? "bg-[var(--warning-text)] hover:opacity-90"
                : "bg-[var(--primary)] hover:bg-[var(--primary-hover)]"
            }`}
          >
            {isLoading
              ? "Processing..."
              : isDowngrade
              ? "Schedule Downgrade"
              : "Confirm Upgrade"}
          </button>
        </div>
      </div>
    </div>
  );
}