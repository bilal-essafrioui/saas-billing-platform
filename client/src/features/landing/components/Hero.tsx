import { ArrowRight } from "lucide-react";
import { NavLink } from "react-router-dom";

export default function Hero() {
  return (
    <section className="mx-auto max-w-[900px] px-8 pb-24 pt-20 text-center">
      <div className="mx-auto mb-6 inline-flex items-center gap-2 rounded-[var(--radius-badge)] border border-[var(--border)] px-4 py-1.5 text-[11px] font-medium tracking-wide text-[var(--text-secondary)]">
        <span className="text-[var(--primary)]">✦</span>
        AUTOMATED BILLING FOR SAAS
      </div>

      <h1 className="text-[44px] font-semibold leading-[1.15] text-[var(--text-primary)] sm:text-[52px]">
        Manage your SaaS billing,
        <br />
        effortlessly
      </h1>

      <p className="mx-auto mt-6 max-w-[520px] text-[17px] text-[var(--text-secondary)]">
        Automatic invoicing, smart retries, zero double billing.
      </p>

      <div className="mt-9 flex items-center justify-center gap-3">
        <NavLink
          to="/register"
          className="inline-flex items-center gap-2 rounded-[var(--radius-button)] bg-[var(--primary)] px-5 py-3 text-sm font-medium !text-white hover:bg-[var(--primary-hover)]"
        >
          Get Started
          <ArrowRight className="h-4 w-4" />
        </NavLink>
        <a
          href="#demo"
          className="rounded-[var(--radius-button)] border border-[var(--border)] px-5 py-3 text-sm font-medium text-[var(--text-primary)] hover:bg-[var(--bg-card)]"
        >
          View Demo
        </a>
      </div>
    </section>
  );
}