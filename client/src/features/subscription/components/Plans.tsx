import { usePlans } from "../hooks/useGetPlans";
import { Check } from "lucide-react";
import { useCheckout } from "../hooks/useCheckout";

type PlansMode = "checkout" | "change";

type Plan = {
  id: string;
  name: string;
  price: number;
  description: string;
  active: boolean;
};

type PlanCardProps = {
  plan: Plan;
  mode: PlansMode;
  currentPlanId?: string;
  currentPlanPrice?: number; // NEW: used only in "change" mode, purely derived, no API impact
  onCheckoutCreated?: (clientSecret: string) => void;
  onPlanSelected?: (planId: string) => void;
};

type TagStyle = "current" | "upgrade" | "downgrade";

const tagStyles: Record<TagStyle, string> = {
  current:
    "border border-[var(--border)] bg-[var(--bg-card-hover)] text-[var(--text-secondary)]",
  upgrade:
    "border border-[var(--border-active)] bg-[var(--primary)]/10 text-[var(--primary)]",
  downgrade:
    "border border-[var(--warning-border)] bg-[var(--warning-bg)] text-[var(--warning-text)]",
};

function PlanCard({
  plan,
  mode,
  currentPlanId,
  currentPlanPrice,
  onCheckoutCreated,
  onPlanSelected,
}: PlanCardProps) {
  const { startCheckout, loading } = useCheckout();

  const isChangeMode = mode === "change";
  const isCurrentPlan = currentPlanId === plan.id;

  const handleAction = async () => {
    if (mode === "checkout") {
      try {
        const response = await startCheckout(plan.id);
        onCheckoutCreated?.(response.clientSecret);
      } catch (e) {
        console.error(e);
      }
      return;
    }

    onPlanSelected?.(plan.id);
  };

  const features = plan.description
    .split("\n")
    .map(f => f.trim())
    .filter(Boolean);

  const borderClass = plan.name === "Pro"
    ? "border-[var(--border-active)]"
    : plan.name === "Enterprise"
    ? "border-[var(--gold-border)]"
    : "border-[var(--border)]";

  // Only relevant in change mode: tells the user if this plan is an
  // upgrade or a downgrade relative to their current subscription.
  const changeTag: { label: string; style: TagStyle } | null = (() => {
    if (!isChangeMode) return null;
    if (isCurrentPlan) return { label: "Current Plan", style: "current" };
    if (currentPlanPrice == null) return null;
    if (plan.price > currentPlanPrice) return { label: "Upgrade", style: "upgrade" };
    if (plan.price < currentPlanPrice) return { label: "Downgrade", style: "downgrade" };
    return null;
  })();

  const changeButtonLabel = isCurrentPlan
    ? "Current Plan"
    : currentPlanPrice == null
    ? "Select"
    : plan.price > currentPlanPrice
    ? `Upgrade to ${plan.name}`
    : plan.price < currentPlanPrice
    ? `Downgrade to ${plan.name}`
    : `Switch to ${plan.name}`;

  return (
    <div
      className={`relative flex w-full flex-col rounded-[var(--radius-card)] border ${borderClass} bg-[var(--bg-card)] ${
        isChangeMode ? "max-w-[340px] p-8" : "max-w-[320px] p-7"
      } ${
        isChangeMode && isCurrentPlan
          ? "ring-2 ring-[var(--primary)]/40"
          : ""
      } ${
        !isChangeMode && plan.name === "Pro"
          ? "shadow-[0_0_0_1px_var(--border-active)]"
          : ""
      }`}
    >
      {/* Checkout-only marketing ribbon — untouched, never shown in change mode */}
      {!isChangeMode && (plan.name === "Pro" || plan.name === "Enterprise") && (
        <span
          className={`absolute -top-3 left-1/2 -translate-x-1/2 rounded-[var(--radius-badge)] px-3 py-1 text-[11px] font-semibold tracking-wide ${
            plan.name === "Pro"
              ? "bg-[var(--primary)] text-[var(--text-button)]"
              : "border border-[var(--gold-border)] bg-[var(--gold-bg)] text-[var(--gold-text)]"
          }`}
        >
          {plan.name === "Pro" ? "MOST POPULAR" : "CUSTOM"}
        </span>
      )}

      {/* Change-mode tag: subtle corner label instead of a heavy banner */}
      {isChangeMode && changeTag && (
        <span
          className={`absolute right-5 top-5 rounded-full px-2.5 py-1 text-[11px] font-semibold tracking-wide ${tagStyles[changeTag.style]}`}
        >
          {changeTag.label}
        </span>
      )}

      <span className="text-[13px] font-semibold tracking-wide text-[var(--text-primary)]">
        {plan.name}
      </span>

      <div className={`flex items-baseline gap-1 ${isChangeMode ? "mt-4" : "mt-3"}`}>
        <span className="text-[34px] font-semibold text-[var(--text-primary)]">
          ${plan.price}
        </span>
        <span className="text-sm text-[var(--text-secondary)]">/mo</span>
      </div>

      <ul className={`flex flex-1 flex-col gap-3 ${isChangeMode ? "mt-7" : "mt-6"}`}>
        {features.map((feature) => (
          <li key={feature} className="flex items-center gap-2 text-sm text-[var(--text-secondary)]">
            <Check
              className={`h-4 w-4 shrink-0 ${
                plan.name === "Enterprise" ? "text-[var(--gold-text)]" : "text-[var(--primary)]"
              }`}
            />
            {feature}
          </li>
        ))}
      </ul>

      <button
        onClick={handleAction}
        disabled={loading || (mode === "change" && isCurrentPlan)}
        className={`w-full rounded-[var(--radius-button)] text-sm font-medium ${
          isChangeMode ? "mt-9 py-3.5" : "mt-8 py-3"
        } ${
          plan.name === "Pro"
            ? "bg-[var(--primary)] text-[var(--text-button)] hover:bg-[var(--primary-hover)] cursor-pointer"
            : "border border-[var(--border)] text-[var(--text-primary)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
        } disabled:cursor-not-allowed disabled:opacity-60`}
      >
        {loading
          ? "Preparing checkout..."
          : mode === "checkout"
          ? `Choose ${plan.name}`
          : changeButtonLabel}
      </button>
    </div>
  );
}

type PlansProps = {
  mode?: PlansMode;
  onCheckoutCreated?: (clientSecret: string) => void;
  onPlanSelected?: (planId: string) => void;
  currentPlanId?: string;
};

export default function Plans({
  mode = "checkout",
  onCheckoutCreated,
  onPlanSelected,
  currentPlanId,
}: PlansProps) {
  const { plans: fetchedPlans, loading, error } = usePlans();

  if (loading) return <p>Loading...</p>;
  if (error) return <p>{error}</p>;

  const isChangeMode = mode === "change";

  // Derived locally from the already-fetched plans list — no extra API call.
  const currentPlanPrice = isChangeMode
    ? fetchedPlans.find((p) => p.id === currentPlanId)?.price
    : undefined;

  return (
    <div
      className={`flex flex-col items-center md:items-stretch md:justify-center ${
        isChangeMode
          ? "mt-8 gap-6 md:flex-row md:gap-6"
          : "mt-14 gap-8 md:flex-row md:gap-8"
      }`}
    >
      {fetchedPlans.map((plan) => (
        <PlanCard
          key={plan.id}
          plan={plan}
          mode={mode}
          currentPlanId={currentPlanId}
          currentPlanPrice={currentPlanPrice}
          onCheckoutCreated={onCheckoutCreated}
          onPlanSelected={onPlanSelected}
        />
      ))}
    </div>
  );
}