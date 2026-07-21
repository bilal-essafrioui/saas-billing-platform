import React from "react";

/**
 * BillFlow loading spinner.
 *
 * Usage:
 *   <Spinner />                          // inline, medium
 *   <Spinner size="sm" />                // inline, small
 *   <Spinner label="Loading invoices…" /> // with text next to it
 *   <Spinner fullScreen />                // centered overlay covering the page
 */

type SpinnerProps = {
  size?: "sm" | "md" | "lg";
  label?: string;
  fullScreen?: boolean;
  className?: string;
};

const sizeMap = {
  sm: "h-4 w-4 border-2",
  md: "h-6 w-6 border-2",
  lg: "h-9 w-9 border-[3px]",
};

export default function Spinner({ size = "md", label, fullScreen = true, className = "" }: SpinnerProps) {
  const spinner = (
    <div className={`flex items-center gap-3 ${className}`}>
      <span
        className={`inline-block animate-spin rounded-full border-[var(--border)] border-t-[var(--primary)] ${sizeMap[size]}`}
      />
      {label && <span className="text-sm text-[var(--text-secondary)]">{label}</span>}
    </div>
  );

  if (!fullScreen) return spinner;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[var(--overlay)]">
      {spinner}
    </div>
  );
}