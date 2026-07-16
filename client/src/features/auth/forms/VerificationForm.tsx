import React, { useEffect, useState } from "react";
import {
  Zap,
  ShieldCheck,
  AlertCircle,
  ArrowRight,
} from "lucide-react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";

export default function VerificationForm() {
  const [otp, setOtp] = useState("");
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // getting email from state passed from register form
  const location = useLocation();
  const email = location.state?.email;

  useEffect(() => {
    if (!email) {
        navigate("/register");
    }
  }, [email, navigate]);
  

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // TODO: Verify OTP
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-[var(--bg-page)] px-4 py-16">
      {/* Logo */}
      <div className="mb-8 flex items-center gap-2">
        <Zap className="h-6 w-6 fill-[var(--primary)] text-[var(--primary)]" />
        <span className="text-2xl font-semibold text-[var(--text-primary)]">
          Bill<span className="text-[var(--primary)]">Flow</span>
        </span>
      </div>

      {/* Card */}
      <div className="w-full max-w-[420px] rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-8 shadow-[var(--shadow-card)]">
        <h1 className="text-center text-[22px] font-semibold text-[var(--text-primary)]">
          Verify your email
        </h1>

        <p className="mt-2 text-center text-sm text-[var(--text-secondary)]">
          We've sent a 6-digit verification code to
        </p>

        <p className="mt-1 text-center text-sm font-medium text-[var(--text-primary)]">
          {email}
        </p>

        {error && (
          <div className="mt-6 flex items-center gap-3 rounded-[var(--radius-card)] border border-[var(--error-border)] bg-[var(--error-bg)] px-4 py-3">
            <AlertCircle className="h-4 w-4 shrink-0 text-[var(--error-text)]" />
            <span className="text-sm text-[var(--error-text)]">{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-5">
          {/* OTP */}
          <div>
            <label
              htmlFor="otp"
              className="mb-2 block text-sm font-medium text-[var(--text-primary)]"
            >
              Verification Code
            </label>

            <div className="relative">
              <ShieldCheck className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />

              <input
                id="otp"
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={otp}
                onChange={(e) =>
                  setOtp(e.target.value.replace(/\D/g, ""))
                }
                placeholder="123456"
                className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-3 text-center text-lg tracking-[0.4em] text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
              />
            </div>

            <p className="mt-2 text-xs text-[var(--text-secondary)]">
              Enter the 6-digit code sent to your email.
            </p>
          </div>

          {/* Submit */}
          <button
            type="submit"
            className="flex w-full cursor-pointer items-center justify-center gap-2 rounded-[var(--radius-button)] bg-[var(--primary)] py-3 text-sm font-semibold text-white hover:bg-[var(--primary-hover)]"
          >
            Verify Email
            <ArrowRight className="h-4 w-4" />
          </button>
        </form>

        {/* Resend */}
        <div className="mt-6 text-center">
          <p className="text-sm text-[var(--text-secondary)]">
            Didn't receive the code?
          </p>

          <button
            className="mt-2 cursor-pointer text-sm font-medium text-[var(--text-link)] hover:underline"
          >
            Resend Code
          </button>
        </div>
      </div>

      {/* Footer */}
      <p className="mt-6 text-sm text-[var(--text-secondary)]">
        Wrong email?{" "}
        <NavLink
          to="/register"
          className="font-medium text-[var(--text-link)] hover:underline"
        >
          Register again
        </NavLink>
      </p>
    </div>
  );
}