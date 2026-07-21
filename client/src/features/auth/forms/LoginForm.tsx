import React, { useState } from "react";
import { Zap, Mail, Lock, Eye, EyeOff, AlertCircle, ArrowRight } from "lucide-react";
import { NavLink } from "react-router-dom";
import { isEmpty, isValidEmail } from "../../../utils/validation";
import { useLogin } from "../hooks/useLogin";
import useAuth from "../../../app/hooks/useAuth";
import {useNavigate} from "react-router-dom";
import { toast } from "sonner";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [validationError, setValidationError] =
    useState<string | null>(null);

  const { signIn, loading, error } = useLogin();

  const navigate = useNavigate();
  const { checkAuth } = useAuth();
  

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setValidationError(null);

    if (isEmpty(email) || isEmpty(password)) {
      setValidationError("Please fill in all fields.");
      return;
    }

    if (!isValidEmail(email)) {
      setValidationError("Please enter a valid email address.");
      return;
    }

    const response = await signIn({
      email,
      password,
    });

    if (!response) return;

    await checkAuth();

    toast.success("Login successful!");

    navigate("/dashboard");
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
          Welcome back
        </h1>
        <p className="mt-1 text-center text-sm text-[var(--text-secondary)]">
          Sign in to your account
        </p>

        {(error || validationError) && (
          <div className="mt-6 flex items-center gap-3 rounded-[var(--radius-card)] border border-[var(--error-border)] bg-[var(--error-bg)] px-4 py-3">
            <AlertCircle className="h-4 w-4 shrink-0 text-[var(--error-text)]" />
            <span className="text-sm text-[var(--error-text)]">
              {error || validationError}
            </span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-5">
          {/* Email */}
          <div>
            <label
              htmlFor="email"
              className="mb-2 block text-sm font-medium text-[var(--text-primary)]"
            >
              Email Address
            </label>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@company.com"
                className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-3 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
              />
            </div>
          </div>

          {/* Password */}
          <div>
            <label
              htmlFor="password"
              className="mb-2 block text-sm font-medium text-[var(--text-primary)]"
            >
              Password
            </label>
            <div className="relative">
              <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--text-muted)]" />
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full rounded-[var(--radius-input)] border border-[var(--border)] bg-[var(--bg-input)] py-3 pl-10 pr-10 text-sm text-[var(--text-primary)] placeholder:text-[var(--text-muted)] outline-none focus:border-[var(--border-active)]"
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-muted)] hover:text-[var(--text-secondary)]"
              >
                {showPassword ? (
                  <EyeOff className="h-4 w-4 cursor-pointer" />
                ) : (
                  <Eye className="h-4 w-4 cursor-pointer" />
                )}
              </button>
            </div>

            <div className="mt-2 text-right">
              <a href="#forgot-password" className="text-sm text-[var(--text-link)] hover:underline">
                Forgot password?
              </a>
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="mt-1 flex w-full items-center justify-center gap-2 rounded-[var(--radius-button)] bg-[var(--primary)] py-3 text-sm font-semibold text-white hover:bg-[var(--primary-hover)] cursor-pointer"
          >
            {loading ? (
              "Signing In..."
            ) : (
              <>
                Sign In
                <ArrowRight className="h-4 w-4" />
              </>
            )}
          </button>
        </form>
      </div>

      {/* Footer link */}
      <p className="mt-6 text-sm text-[var(--text-secondary)]">
        Don&apos;t have an account?{" "}
        <NavLink to="/register" className="font-medium text-[var(--text-link)] hover:underline">
          Register
        </NavLink>
      </p>
    </div>
  );
}