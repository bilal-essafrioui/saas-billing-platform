// checkout response type
export type CheckoutResponse = {
  clientSecret: string;
  paymentId: string;
  paymentIntentId: string;
};