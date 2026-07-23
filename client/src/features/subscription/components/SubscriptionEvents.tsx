import { useEffect } from "react";
import { useSubscriptionHistory } from "../hooks/useSubscriptionHistory";
import { toast } from "sonner";
import {
  ArrowUpCircle,
  ArrowDownCircle,
  RefreshCw,
  PauseCircle,
  XCircle,
  PlusCircle,
  Clock,
  RotateCcw,
  type LucideIcon,
} from "lucide-react";

import type { SubscriptionEventType, SubscriptionStatus } from "../types/subscription.types";

// Icon + soft text color per event type — no background pill, no border.


const eventTypeMeta: Record<SubscriptionEventType, { icon: LucideIcon; color: string; label: string }> = {
  SUBSCRIBED: { icon: PlusCircle, color: "var(--success-text)", label: "Subscribed" },
  UPGRADED: { icon: ArrowUpCircle, color: "var(--success-text)", label: "Upgraded" },
  DOWNGRADE_SCHEDULED: { icon: Clock, color: "var(--warning-text)", label: "Downgrade Scheduled" },
  DOWNGRADE_APPLIED: { icon: ArrowDownCircle, color: "var(--warning-text)", label: "Downgrade Applied" },
  DOWNGRADE_CANCELLED: { icon: XCircle, color: "var(--primary)", label: "Downgrade Cancelled" },
  STATUS_CHANGED: { icon: RefreshCw, color: "var(--primary)", label: "Status Changed" },
  CANCELLED: { icon: XCircle, color: "var(--text-muted)", label: "Cancelled" },
  RESUBSCRIBED: { icon: RotateCcw, color: "var(--success-text)", label: "Resubscribed" },
  SUSPENSION: { icon: PauseCircle, color: "var(--error-text)", label: "Suspension" },
};


// Plain text colors for status — used inline, never as a filled badge.
const statusTextColor: Record<SubscriptionStatus, string> = {
  ACTIVE: "var(--success-text)",
  PAST_DUE: "var(--warning-text)",
  SUSPENDED: "var(--error-text)",
  CANCELLED: "var(--text-muted)",
};

const statusLabel: Record<SubscriptionStatus, string> = {
  ACTIVE: "Active",
  PAST_DUE: "Past Due",
  SUSPENDED: "Suspended",
  CANCELLED: "Cancelled",
};

function formatMoney(value: number | null) {
  if (value === null) return "—";
  return `${value.toFixed(2)}`;
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

export default function SubscriptionEvents() {
  const { events, loading, error, fetchSubscriptionHistory,} = useSubscriptionHistory();
  
  useEffect(() => {
    fetchSubscriptionHistory();
  }, []);

  if (loading) {
    return;
  }

  if (error) {
    return toast.error(error.message);
  }
  return (
    <div className="mt-4 rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-5">
      <h2 className="text-base font-semibold text-[var(--text-primary)]">Subscription History</h2>

      <div className="mt-4 overflow-x-auto">
        <table className="w-full min-w-[860px] border-collapse text-sm">
          <thead>
            <tr className="border-b border-[var(--border)] text-left text-[11px] uppercase tracking-wide text-[var(--text-muted)]">
              <th className="pb-3 pr-4 font-medium">Date</th>
              <th className="pb-3 pr-4 font-medium">Event</th>
              <th className="pb-3 pr-4 font-medium">Plan</th>
              <th className="pb-3 pr-4 font-medium">Status</th>
              <th className="pb-3 pr-4 text-right font-medium">Prorata</th>
              <th className="pb-3 font-medium">Note</th>
            </tr>
          </thead>
          <tbody>
            {events.map((event) => {
              const meta = eventTypeMeta[event.eventType];
              const Icon = meta.icon;

              return (
                <tr
                  key={event.id}
                  className="border-b border-[var(--border)] last:border-0 hover:bg-[var(--bg-card-hover)]"
                >
                  <td className="py-4 pr-4 align-top whitespace-nowrap text-[var(--text-secondary)]">
                    {formatDate(event.createdAt)}
                  </td>

                  <td className="py-4 pr-4 align-top">
                    <span className="inline-flex items-center gap-2">
                      <Icon className="h-4 w-4 shrink-0" style={{ color: meta.color }} />
                      <span className="font-medium text-[var(--text-primary)]">{meta.label}</span>
                    </span>
                  </td>

                  <td className="py-4 pr-4 align-top whitespace-nowrap">
                    {event.previousPlanName ? (
                      <>
                        <div className="flex items-center gap-1.5">
                          <span className="text-[var(--text-muted)]">{event.previousPlanName}</span>
                          <span className="text-[var(--text-muted)]">→</span>
                          <span className="font-medium text-[var(--text-primary)]">{event.newPlanName}</span>
                        </div>
                        <div className="mt-0.5 text-xs text-[var(--text-muted)]">
                          ${formatMoney(event.previousPlanPrice)} → ${formatMoney(event.newPlanPrice)}
                        </div>
                      </>
                    ) : (
                      <>
                        <span className="font-medium text-[var(--text-primary)]">{event.newPlanName}</span>
                        <div className="mt-0.5 text-xs text-[var(--text-muted)]">
                          ${formatMoney(event.newPlanPrice)}
                        </div>
                      </>
                    )}
                  </td>

                  <td className="py-4 pr-4 align-top whitespace-nowrap">
                    <span className="inline-flex items-center gap-1.5">
                      {event.previousStatus && (
                        <>
                          <span style={{ color: statusTextColor[event.previousStatus] }}>
                            {statusLabel[event.previousStatus]}
                          </span>
                          <span className="text-[var(--text-muted)]">→</span>
                        </>
                      )}
                      <span className="font-medium" style={{ color: statusTextColor[event.newStatus] }}>
                        {statusLabel[event.newStatus]}
                      </span>
                    </span>
                  </td>

                  <td className="py-4 pr-4 align-top whitespace-nowrap text-right text-[var(--text-secondary)]">
                    {event.prorataAmount !== null ? `+${formatMoney(event.prorataAmount)}` : "—"}
                  </td>

                  <td className="max-w-[220px] py-4 align-top text-[var(--text-secondary)]">
                    <span className="line-clamp-1">{event.note ?? "—"}</span>
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