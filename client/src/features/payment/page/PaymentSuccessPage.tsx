import { useEffect, useState } from "react";
import { Zap, Check } from "lucide-react";
import { useNavigate } from "react-router-dom";

/**
 * BillFlow "/payment/success" page.
 * Shows an animated success state, then automatically redirects to /dashboard
 * after a short delay (default 4s). A manual "Go to dashboard" button lets
 * the user skip the wait.
 */

const REDIRECT_DELAY_SECONDS = 5;

export default function PaymentSuccessPage() {
  const navigate = useNavigate();
  const [secondsLeft, setSecondsLeft] = useState(REDIRECT_DELAY_SECONDS);

  useEffect(() => {
    if (secondsLeft <= 0) {
      navigate("/dashboard");
      return;
    }
    const timer = setTimeout(() => setSecondsLeft((s) => s - 1), 1000);
    return () => clearTimeout(timer);
  }, [secondsLeft, navigate]);

  const progress = ((REDIRECT_DELAY_SECONDS - secondsLeft) / REDIRECT_DELAY_SECONDS) * 100;

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-[var(--bg-page)] px-4">
      {/* Logo */}
      <div className="mb-10 flex items-center gap-2">
        <Zap className="h-5 w-5 fill-[var(--warning-text)] text-[var(--warning-text)]" />
        <span className="text-lg font-semibold text-white">
          Bill<span className="text-[var(--primary)]">Flow</span>
        </span>
      </div>

      <div className="flex w-full max-w-[420px] flex-col items-center rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-10 text-center shadow-[var(--shadow-card)]">
        {/* Animated checkmark */}
        <div className="relative flex h-20 w-20 items-center justify-center">
          <span className="absolute inset-0 animate-ping rounded-full bg-[var(--success-text)] opacity-20" />
          <span className="relative flex h-20 w-20 items-center justify-center rounded-full border border-[var(--success-border)] bg-[var(--success-bg)]">
            <Check className="h-9 w-9 animate-[check-pop_0.4s_ease-out] text-[var(--success-text)]" strokeWidth={3} />
          </span>
        </div>

        <h1 className="mt-6 text-[22px] font-semibold text-[var(--text-primary)]">
          Payment successful
        </h1>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">
          Your subscription is now active. Welcome to BillFlow.
        </p>

        {/* Progress bar */}
        <div className="mt-7 h-1 w-full overflow-hidden rounded-full bg-[var(--border)]">
          <div
            className="h-full rounded-full bg-[var(--primary)] transition-[width] duration-1000 ease-linear"
            style={{ width: `${progress}%` }}
          />
        </div>

        <p className="mt-3 text-xs text-[var(--text-muted)]">
          Redirecting to your dashboard in {secondsLeft}s…
        </p>

        <button
          type="button"
          onClick={() => navigate("/dashboard")}
          className="mt-6 w-full rounded-[var(--radius-button)] bg-[var(--primary)] py-3 text-sm font-semibold text-white hover:bg-[var(--primary-hover)]"
        >
          Go to dashboard now
        </button>
      </div>

      {/* Local keyframes for the checkmark pop-in */}
      <style>{`
        @keyframes check-pop {
          0% { transform: scale(0); opacity: 0; }
          70% { transform: scale(1.15); opacity: 1; }
          100% { transform: scale(1); opacity: 1; }
        }
      `}</style>
    </div>
  );
}