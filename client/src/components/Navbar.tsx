import { Zap } from "lucide-react";
import { NavLink } from "react-router-dom";

export default function Navbar() {
  return (
    <header className="border-b border-[var(--border)]">
      <div className="mx-auto flex max-w-[1600px] items-center justify-between px-8 py-5">
        <div className="flex items-center gap-2">
          <Zap className="h-5 w-5 fill-[var(--warning-text)] text-[var(--warning-text)]" />
          <span
            className="text-[17px] font-semibold text-[var(--text-primary)]"
          >
            Bill<span className="text-[var(--primary)]">Flow</span>
          </span>
        </div>

        <nav className="hidden items-center gap-8 md:flex">
          <NavLink to="/" className="text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
            Home
          </NavLink>
          <a href="#pricing" className="text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
            Pricing
          </a>
          <a href="#contact" className="text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
            Contact
          </a>
        </nav>

        <div className="flex items-center gap-6">
          <NavLink to="/login" className="text-sm text-[var(--text-primary)]">
            Log In
          </NavLink>
          <NavLink
            to="/register"
            className="rounded-[var(--radius-button)] bg-[var(--primary)] px-4 py-2 text-sm font-medium !text-white hover:bg-[var(--primary-hover)]"
          >
            Sign Up
          </NavLink>
        </div>
      </div>
    </header>
  );
}