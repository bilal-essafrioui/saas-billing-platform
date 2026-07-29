import { PaymentElement } from "@stripe/react-stripe-js";
import { useStripeSetup } from "../hooks/useStripeSetup";
import { useEffect, type FormEvent } from "react";
import { toast } from "sonner";

type UpdateCardFormProps = {
  onSuccess: () => void;
};

export default function UpdateCardForm({
  onSuccess,
}: UpdateCardFormProps) {
  const {
    loading,
    error,
    confirmSetup,
    stripe,
  } = useStripeSetup();

  useEffect(() => {
    if (error) {
      toast.error(error);
    }
  }, [error]);

  async function handleSubmit(e: FormEvent) {
    const success = await confirmSetup(e);

    if (success) {
      onSuccess();
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
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
        {loading
          ? "Updating..."
          : "Update payment method"}
      </button>
    </form>
  );
}