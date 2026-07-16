import Plans from "../../subscription/components/Plans";

export default function Pricing() {
  return (
    <section id="pricing" className="px-8 pb-28">
      <div className="mx-auto max-w-[1100px] text-center">
        <span className="text-[13px] font-semibold tracking-wide text-[var(--primary)]">
          PRICING
        </span>
        <h2 className="mt-2 text-[26px] font-semibold text-[var(--text-primary)]">
          Simple, transparent pricing
        </h2>

        <Plans />
        
      </div>
    </section>
  );
}