import { Check } from "lucide-react";

/**
 * Reusable, functional step indicator.
 * Pass the list of step labels and the current step (1-indexed).
 * Status per step (completed / current / upcoming) is derived automatically,
 * so this works for any flow with any number of steps.
 */

export type Step = {
  label: string;
};

type StepperProps = {
  steps: Step[];
  currentStep: number; // 1-indexed
  onStepClick?: (step: number) => void; // optional: allow navigating back to completed steps
};

export default function Stepper({ steps, currentStep, onStepClick }: StepperProps) {
  return (
    <div className="flex items-center justify-center">
      {steps.map((step, index) => {
        const stepNumber = index + 1;
        const isCompleted = stepNumber < currentStep;
        const isCurrent = stepNumber === currentStep;
        const isClickable = isCompleted && !!onStepClick;

        return (
          <div key={step.label} className="flex items-center">
            <div className="flex flex-col items-center gap-2">
              <button
                type="button"
                disabled={!isClickable}
                onClick={() => isClickable && onStepClick?.(stepNumber)}
                className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-semibold transition-colors ${
                  isCompleted
                    ? "bg-[var(--text-primary)] text-[var(--bg-page)]"
                    : isCurrent
                    ? "bg-[var(--primary)] text-white"
                    : "border border-[var(--border)] bg-transparent text-[var(--text-muted)]"
                } ${isClickable ? "cursor-pointer" : "cursor-default"}`}
              >
                {isCompleted ? <Check className="h-3.5 w-3.5" /> : stepNumber}
              </button>

              <span
                className={`text-xs ${
                  isCurrent
                    ? "font-medium text-[var(--primary)]"
                    : isCompleted
                    ? "text-[var(--text-primary)]"
                    : "text-[var(--text-muted)]"
                }`}
              >
                {step.label}
              </span>
            </div>

            {stepNumber < steps.length && (
              <span
                className={`mx-3 mb-5 h-px w-16 ${
                  isCompleted ? "bg-[var(--text-primary)]" : "bg-[var(--border)]"
                }`}
              />
            )}
          </div>
        );
      })}
    </div>
  );
}