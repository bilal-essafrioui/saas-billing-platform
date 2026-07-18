import { usePlans } from "../hooks/useGetPlans";
import { Check } from "lucide-react";
import { useCheckout } from "../hooks/useCheckout";

type Plan = {
  id: string;
  name: string;
  price: number;
  description: string;
  active: boolean;
};

type PlanCardProps = {
  plan: Plan;
  onCheckoutCreated: (clientSecret: string) => void;
};

function PlanCard({ plan, onCheckoutCreated,}: PlanCardProps) {
  const { startCheckout, loading } = useCheckout();
  const handleCheckout = async () => {
    try {
          const response = await startCheckout(plan.id);

          console.log(response);

          // next step:
          onCheckoutCreated(response.clientSecret);

      } catch (e) {
          console.error(e);
      }
  };

  const features = plan.description
    .split("\n")
    .map(f => f.trim())
    .filter(Boolean);

  const borderClass = plan.name === "Pro"
    ? "border-[var(--border-active)]"
    : plan.name === "Entreprise"
    ? "border-[var(--gold-border)]"
    : "border-[var(--border)]";

  return (
    <div
      className={`relative flex w-full max-w-[320px] flex-col rounded-[var(--radius-card)] border ${borderClass} bg-[var(--bg-card)] p-7 ${
        plan.name === "Pro" ? "shadow-[0_0_0_1px_var(--border-active)]" : ""
      }`}
    >
      {(plan.name === "Pro" || plan.name === "Enterprise") && (
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

      <span className="text-[13px] font-semibold tracking-wide text-[var(--text-primary)]">
        {plan.name}
      </span>

      <div className="mt-3 flex items-baseline gap-1">
        <span className="text-[34px] font-semibold text-[var(--text-primary)]">
          ${plan.price}
        </span>
        <span className="text-sm text-[var(--text-secondary)]">/mo</span>
      </div>

      <ul className="mt-6 flex flex-1 flex-col gap-3">
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
        onClick={handleCheckout}
        disabled={loading}
        className={`mt-8 w-full rounded-[var(--radius-button)] py-3 text-sm font-medium ${
          plan.name === "Pro"
            ? "bg-[var(--primary)] text-[var(--text-button)] hover:bg-[var(--primary-hover)] cursor-pointer"
            : "border border-[var(--border)] text-[var(--text-primary)] hover:bg-[var(--bg-card-hover)] cursor-pointer"
        }`}
      >
        {loading
          ? "Preparing checkout..."
          : `choose ${plan.name.toLowerCase()}`}
      </button>
    </div>
  );
}

type PlansProps = {
  onCheckoutCreated: (clientSecret: string) => void;
};

export default function Plans({ onCheckoutCreated,}: PlansProps) {
  const { plans: fetchedPlans, loading, error } = usePlans();
 
  if (loading) return <p>Loading...</p>;
  if (error) return <p>{error}</p>;


  
  return (
    <div className="mt-14 flex flex-col items-center gap-8 md:flex-row md:items-stretch md:justify-center">
        {fetchedPlans.map((plan) => (
        <PlanCard
            key={plan.id}
            plan={plan}
            onCheckoutCreated={onCheckoutCreated}
        />
        ))}
    </div>
  );
}