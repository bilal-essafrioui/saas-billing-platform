import React from "react";
import {useNavigate} from "react-router-dom";
import { Calendar, CheckCircle2, Zap, AlertTriangle, Ban, XCircle } from "lucide-react";
import { useMySubscription } from "../../subscription/hooks/useMySubscription";
import Spinner from "../../../components/ui/Spinner";
import useAuth from "../../../app/hooks/useAuth";
import { toast } from "sonner";
import RecentInvoices from "../../invoice/component/RecentInvoices";
import type { SubscriptionStatus } from "../../subscription/types/subscription.types";


export type Activity = {
  id: string;
  title: string;
  subtitle: string;
  tone: "success" | "info" | "warning" | "error";
};


const subscriptionStatusConfig: Record<
  SubscriptionStatus,
  { label: string; bg: string; text: string; border: string; description: string; icon: React.ReactNode }
> = {
  ACTIVE: {
    label: "ACTIVE",
    bg: "var(--success-bg)",
    text: "var(--success-text)",
    border: "var(--success-border)",
    description: "All payments up to date",
    icon: <CheckCircle2 className="h-4 w-4" />,
  },
  PAST_DUE: {
    label: "PAST DUE",
    bg: "var(--warning-bg)",
    text: "var(--warning-text)",
    border: "var(--warning-border)",
    description: "Payment failed — retrying automatically",
    icon: <AlertTriangle className="h-4 w-4" />,
  },
  SUSPENDED: {
    label: "SUSPENDED",
    bg: "var(--error-bg)",
    text: "var(--error-text)",
    border: "var(--error-border)",
    description: "Account suspended after failed retries",
    icon: <Ban className="h-4 w-4" />,
  },
  CANCELLED: {
    label: "CANCELLED",
    bg: "var(--bg-card-hover)",
    text: "var(--text-muted)",
    border: "var(--border)",
    description: "Subscription has been cancelled",
    icon: <XCircle className="h-4 w-4" />,
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

export default function DashboardPage() {
  const { subscription, loading, error } = useMySubscription();
  const { user } = useAuth();
  const navigate = useNavigate();

  // 👇 Put them HERE
  const renewalDate = subscription?.nextRenewalDate
    ? new Date(subscription.nextRenewalDate)
    : null;

  const formattedRenewalDate = renewalDate
    ? renewalDate.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    : "N/A";

  const today = new Date();

  const daysRemaining = renewalDate
    ? Math.max(
        0,
        Math.ceil(
          (renewalDate.getTime() - today.getTime()) /
            (1000 * 60 * 60 * 24)
        )
      )
    : 0;


   const currentDate = new Date();

    const dFormattedCurrentDate = currentDate.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
    });

  const isCancellationPending =
  subscription?.status === "CANCELLED" &&
  subscription.nextRenewalDate &&
  new Date(subscription.nextRenewalDate) > new Date();

  const currentStatus = isCancellationPending
    ? "ACTIVE"
    : (subscription?.status ?? "ACTIVE");

  const statusConfig = isCancellationPending
    ? {
        ...subscriptionStatusConfig.ACTIVE,
        description: `Subscription cancelled — active until ${new Date(
          subscription!.nextRenewalDate
        ).toLocaleDateString("en-US", {
          month: "short",
          day: "numeric",
          year: "numeric",
        })}`,
      }
    : subscriptionStatusConfig[currentStatus];


  if (loading) {
    return <Spinner />;
  }

  if (error) {
    toast.error(error.message);
    navigate("/choose-plan", {
        replace: true,
        state: {
          email: user?.email,
        },
      });
    return null;
  }

  return (
    <div>
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-[28px] font-semibold text-[var(--text-primary)]">Dashboard</h1>
          <p className="mt-1 text-sm text-[var(--text-secondary)]">{dFormattedCurrentDate}</p>
        </div>

      </div>

      {/* Top row: plan / renewal / status */}
      <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
        {/* Current plan */}
        <div className="relative overflow-hidden rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium tracking-wide text-[var(--text-secondary)]">
              CURRENT PLAN
            </span>
            <StatusBadge
              label={subscription?.planName ?? "N/A"}
              bg="var(--primary-glow)"
              text="var(--primary)"
              border="var(--border-active)"
            />
          </div>
          <p className="mt-3 text-2xl font-semibold text-[var(--text-primary)]">
            ${subscription?.planPrice.toFixed(2) ?? "0.00"}
            <span className="ml-1 text-sm font-normal text-[var(--text-secondary)]">/ month</span>
          </p>
          <Zap className="pointer-events-none absolute -bottom-3 -right-3 h-20 w-20 text-[var(--border)] opacity-40" />
        </div>

        {/* Next renewal */}
        <div className="rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium tracking-wide text-[var(--text-secondary)]">
              NEXT RENEWAL
            </span>
            <Calendar className="h-4 w-4 text-[var(--text-muted)]" />
          </div>
          <p className="mt-3 text-2xl font-semibold text-[var(--text-primary)]">{formattedRenewalDate}</p>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-[var(--primary)]">
            <Calendar className="h-3.5 w-3.5" />
            {daysRemaining} days remaining
          </p>
        </div>

        {/* Status */}
        <div className="rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium tracking-wide text-[var(--text-secondary)]">
              STATUS
            </span>
            <StatusBadge
              label={statusConfig.label}
              bg={statusConfig.bg}
              text={statusConfig.text}
              border={statusConfig.border}
            />
          </div>
          <p className="mt-3 text-2xl font-semibold text-[var(--text-primary)]">
            {isCancellationPending
              ? "Cancellation Scheduled"
              : currentStatus === "ACTIVE"
                ? "All Good"
                : statusConfig.label}
          </p>
          <p className="mt-1 flex items-center gap-1.5 text-sm" style={{ color: statusConfig.text }}>
            {statusConfig.icon}
            {statusConfig.description}
          </p>
        </div>
      </div>

      {/* Bottom row: invoices / activity */}
      <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-[1.4fr_1fr]">
        {/* Recent invoices */}
        <RecentInvoices />

        {/* Card */}
        <div className="rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
            <h2 className="text-base font-semibold text-[var(--text-primary)]">Upcoming Billing</h2>
            <p className="mt-2 text-sm text-[var(--text-secondary)]">
                Your next charge will be processed automatically.
            </p>

            <p className="mt-5 text-3xl font-semibold text-[var(--text-primary)]">
                {subscription?.planPrice.toFixed(2) ?? "0.00"}
                <span className="ml-1.5 text-sm font-normal text-[var(--text-secondary)]">USD</span>
            </p>

            <div className="mt-5 flex items-center gap-2 rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-card-hover)] px-3 py-2.5 text-sm text-[var(--text-secondary)]">
                <Calendar className="h-4 w-4 text-[var(--text-muted)]" />
                Scheduled for {formattedRenewalDate}
            </div>
            </div>
        </div>
    </div>
  );
}