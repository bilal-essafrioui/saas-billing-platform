import { useState } from "react";
import { Zap } from "lucide-react";
import Stepper from "../../../components/ui/Stepper";
import type { Step } from "../../../components/ui/Stepper";
import Plans from "../components/Plans";
import { stripePromise } from "../../../lib/stripe";
import CheckoutForm from "../../payment/component/CheckoutForm";
import { Elements } from "@stripe/react-stripe-js";


const steps: Step[] = [{ label: "Account" }, { label: "Choose Plan" }, { label: "Payment" }];

type ChoosePlanPageProps = {
  currentStep?: number;
  onStepClick?: (step: number) => void;
};

export default function ChoosePlanPage({ currentStep = 2, onStepClick }: ChoosePlanPageProps) {
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  return (
    <div className="min-h-screen bg-[var(--bg-page)] px-4 py-16">
      <div className="mb-9 flex items-center justify-center gap-2">
        <Zap className="h-5 w-5 fill-[var(--warning-text)] text-[var(--warning-text)]" />
        <span className="text-lg font-semibold text-white">
          Bill<span className="text-[var(--primary)]">Flow</span>
        </span>
      </div>

      <Stepper
        steps={steps}
        currentStep={clientSecret ? 3 : 2}
        onStepClick={onStepClick}
      />

      <div className="mx-auto mt-10 max-w-[640px] text-center">
        <h1>
          {clientSecret
              ? "Complete your payment"
              : "Choose your plan"}
        </h1>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">
          {clientSecret
              ? "Your subscription will be activated immediately after payment."
              : "Change or cancel at any time. Transparent pricing for high-performance teams."}
        </p>
      </div>
      
      {
        !clientSecret ? (
          <Plans onCheckoutCreated={setClientSecret} />
        ) : (
          <div className="mx-auto mt-10 max-w-[640px]">
          <Elements
            stripe={stripePromise}
            options={{
              clientSecret,
              locale: "en",
              appearance: {
                theme: "night",

                variables: {
                  colorPrimary: "#5b5ef4",
                  colorBackground: "#1a1a24",
                  colorText: "#eeeef5",
                  colorTextPlaceholder: "#9898b0",
                  colorDanger: "#f87171",
                  borderRadius: "7px",
                  fontFamily: "Inter, sans-serif",
                  spacingUnit: "4px",
                },

                rules: {
                  ".Input": {
                    backgroundColor: "#13131a",
                    border: "1px solid #252535",
                    color: "#eeeef5",
                    boxShadow: "none",
                  },

                  ".Input:focus": {
                    border: "1px solid #5b5ef4",
                    boxShadow: "0 0 0 3px rgba(91,94,244,.2)",
                  },

                  ".Label": {
                    color: "#9898b0",
                  },

                  ".Tab": {
                    backgroundColor: "#13131a",
                    border: "1px solid #252535",
                    color: "#eeeef5",
                  },

                  ".Tab--selected": {
                    borderColor: "#5b5ef4",
                    backgroundColor: "#1f1f2e",
                  },

                  ".Block": {
                    backgroundColor: "#1a1a24",
                  },

                  ".Error": {
                    color: "#f87171",
                  },
                },
              },
            }}
          >
          
            <CheckoutForm />
          </Elements>
          </div>
        )
      }
    </div>
  );
}