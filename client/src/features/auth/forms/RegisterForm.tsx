import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Zap, User, Mail, Lock, ShieldCheck, Eye, EyeOff, ArrowRight } from "lucide-react";
import { NavLink } from "react-router-dom";
import { toast } from "sonner";
import { validateRegisterForm } from "../../../utils/validation"
import { useRegister } from "../hooks/useRegister";

type Strength = 0 | 1 | 2 | 3 | 4;

function getStrength(password: string): Strength {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return Math.min(score, 4) as Strength;
}

const strengthColor: Record<Strength, string> = {
  0: "var(--border)",
  1: "var(--error-text)",
  2: "var(--warning-text)",
  3: "var(--gold-text)",
  4: "var(--success-text)",
};

export default function RegisterPage() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const { submit, loading, error } = useRegister();

  const strength = useMemo(() => getStrength(password), [password]);

  useEffect(() => {
    if (error) {
      toast.error(error);
    }
  }, [error]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const error = validateRegisterForm({
      firstName,
      lastName,
      email,
      password,
      confirmPassword,
    });

    if (error) {
      toast.error(error);
      return;
    }

    const response = await submit({
      firstName,
      lastName,
      email,
      password,
    });

    if (response) {
      toast.success(response.message);
      navigate("/verify-email", {
        replace: true,
        state: {
          email: response?.email,
        },
      });
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-[var(--bg-page)] px-4 py-16">
      <div className="w-full max-w-[460px] rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--bg-card)] p-8 shadow-[var(--shadow-card)]">
        {/* Logo */}
        <div className="flex items-center justify-center gap-2">
          <Zap className="h-5 w-5 fill-[var(--warning-text)] text-[var(--warning-text)]" />
          <span className="text-lg font-semibold text-[var(--text-primary)]">
            Bill <span className="text-[var(--primary)]">Flow</span>
          </span>
        </div>

        <h1 className="mt-4 text-center text-[22px] font-semibold text-[var(--text-primary)]">
          Create your account
        </h1>
        <p className="mt-1 text-center text-sm text-[var(--text-secondary)]">
          Start your subscription today
        </p>

        <form onSubmit={handleSubmit} className="mt-7 flex flex-col gap-4">
          {/* First name */}
          <div className="relative">
            <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="First name"
              className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
            />
          </div>

          {/* Last name */}
          <div className="relative">
            <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Last name"
              className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
            />
          </div>

          {/* Email */}
          <div className="relative">
            <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
            <input
              type="text"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email address"
              className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
            />
          </div>

          {/* Password */}
          <div>
            <div className="relative">
              <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
                className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-10 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] hover:text-[var(--text-secondary)]"
              >
                {showPassword ? <EyeOff className="h-4 w-4 cursor-pointer" /> : <Eye className="h-4 w-4 cursor-pointer" />}
              </button>
            </div>

            {/* Strength meter */}
            <div className="mt-2 grid grid-cols-4 gap-1.5">
              {[1, 2, 3, 4].map((segment) => (
                <span
                  key={segment}
                  className="h-1 rounded-full transition-colors"
                  style={{
                    backgroundColor:
                      segment <= strength ? strengthColor[strength] : "var(--border)",
                  }}
                />
              ))}
            </div>
          </div>

          {/* Confirm password */}
          <div className="relative">
            <ShieldCheck className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
            <input
              type={showPassword ? "text" : "password"}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirm password"
              className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
            />
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="mt-2 flex w-full items-center justify-center gap-2 rounded-[var(--radius-button)] bg-[var(--primary)] py-3 text-sm font-semibold text-white hover:bg-[var(--primary-hover)] cursor-pointer"
          >
            {loading ? (
              "Creating account..."
            ) : (
              <>
                Create Account
                <ArrowRight className="h-4 w-4" />
              </>
            )}
          </button>
        </form>

        {/* Divider */}
        <div className="mt-6 flex items-center gap-4">
          <span className="h-px flex-1 bg-[var(--border)]" />
          <span className="text-xs text-[var(--text-muted)]">or</span>
          <span className="h-px flex-1 bg-[var(--border)]" />
        </div>

        {/* Sign in */}
        <p className="mt-6 text-center text-sm text-[var(--text-secondary)]">
          Already have an account?{" "}
          <NavLink to="/login" className="font-medium text-[var(--text-link)] hover:underline">
            Sign in
          </NavLink>
        </p>
      </div>
    </div>
  );
}