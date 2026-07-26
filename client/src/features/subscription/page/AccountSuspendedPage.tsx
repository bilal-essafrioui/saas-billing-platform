import { useLocation } from "react-router-dom";
import { Zap, Ban, CreditCard, LifeBuoy, Lock } from "lucide-react";

/**
 * BillFlow "Account Suspended" page.
 * Shown when a subscription has moved to SUSPENDED after all automatic
 * retry attempts (Day +1, Day +3, Day +7) have failed.
 */

type RetryAttempt = {
  date: string; // e.g. "Jul 8, 2026"
};

type AccountSuspendedPageProps = {
  planName?: string;
  lastFourDigits?: string;
  retryAttempts?: RetryAttempt[];
  onUpdatePaymentMethod?: () => void;
  onContactSupport?: () => void;
};




function formatDate(date: string) {
  return new Date(date).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function addDays(date: string, days: number) {
  const result = new Date(date);
  result.setDate(result.getDate() + days);

  return formatDate(result.toISOString());
}


export default function AccountSuspendedPage({
  onUpdatePaymentMethod,
  onContactSupport,
}: AccountSuspendedPageProps) {
    const location = useLocation();
    const { nextRenewalDate, planName,} = location.state || {};

    const retryAttempts = nextRenewalDate
    ? [
        { date: addDays(nextRenewalDate, 1) },
        { date: addDays(nextRenewalDate, 3) },
        { date: addDays(nextRenewalDate, 7) },
      ]
    : [];
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-[var(--bg-page)] px-4 py-16">
      {/* Logo */}
      <div className="mb-10 flex items-center gap-2">
        <Zap className="h-5 w-5 fill-[var(--warning-text)] text-[var(--warning-text)]" />
        <span className="text-lg font-semibold text-white">
          Bill<span className="text-[var(--primary)]">Flow</span>
        </span>
      </div>

      <div className="w-full max-w-[480px] rounded-[var(--radius-card)] border border-[var(--error-border)] bg-[var(--bg-card)] p-9 text-center shadow-[var(--shadow-card)]">
        {/* Icon */}
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full border border-[var(--error-border)] bg-[var(--error-bg)]">
          <Lock className="h-7 w-7 text-[var(--error-text)]" strokeWidth={2} />
        </div>

        <h1 className="mt-6 text-[22px] font-semibold text-[var(--text-primary)]">
          Account Suspended
        </h1>
        <p className="mt-2 text-sm leading-relaxed text-[var(--text-secondary)]">
          Your subscription has been suspended due to consecutive payment failures. Please
          update your billing information to restore access.
        </p>

        {/* Retry timeline */}
        <div className="mt-6 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-input)] p-4 text-left">
          <p className="mb-3 text-xs font-medium tracking-wide text-[var(--text-muted)]">
            PAYMENT RETRY HISTORY
          </p>
          <ul className="flex flex-col gap-3">
            {retryAttempts.map((attempt, index) => (
              <li key={attempt.date + index} className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span
                    className={"flex h-5 w-5 items-center justify-center rounded-full bg-[var(--error-bg)] text-[var(--error-text)]"}
                  >
                      <Ban className="h-3 w-3" strokeWidth={2.5} />
                  </span>
                  <span className="text-sm text-[var(--text-primary)]">Payment attempt</span>
                </div>
                <span className="text-xs text-[var(--text-muted)]">{attempt.date}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* CTAs */}
        <div className="mt-7 flex flex-col gap-3">
          <button
            type="button"
            onClick={onUpdatePaymentMethod}
            className="flex w-full items-center justify-center gap-2 rounded-[var(--radius-button)] bg-[var(--primary)] py-3 text-sm font-semibold text-white hover:bg-[var(--primary-hover)] cursor-pointer"
          >
            <CreditCard className="h-4 w-4" />
            Update Payment Method
          </button>

          <button
            type="button"
            onClick={onContactSupport}
            className="flex w-full items-center justify-center gap-2 rounded-[var(--radius-button)] border border-[var(--border)] py-3 text-sm font-medium text-[var(--text-primary)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
          >
            <LifeBuoy className="h-4 w-4" />
            Contact Support
          </button>
        </div>
      </div>

      <p className="mt-6 max-w-[420px] text-center text-xs text-[var(--text-muted)]">
        Your data is safe and will remain available for 30 days. Reactivating your payment
        method will instantly restore full access.
      </p>
    </div>
  );
}