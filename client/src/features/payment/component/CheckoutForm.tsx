import { PaymentElement } from "@stripe/react-stripe-js";
import { useStripeCheckout  } from "../hooks/useStripeCheckout";
import { toast } from "sonner";
import { useEffect } from "react";

export default function CheckoutForm() {
  const {
    loading,
    error,
    confirmPayment,
    stripe,
  } = useStripeCheckout();

  useEffect(() => {
      if (error) {
          toast.error(error);
      }
  }, [error]);

  return (
    <form
      onSubmit={confirmPayment}
      className="space-y-6"
    >
      <PaymentElement 
        options={{
            wallets: {
            link: "never",
            },
        }}
      />

      <button
        type="submit"
        disabled={!stripe || loading}
        className="w-full rounded-lg bg-[var(--primary)] py-3 text-white disabled:opacity-50"
      >
        {loading ? "Processing..." : "Pay now"}
      </button>
    </form>
  );
}