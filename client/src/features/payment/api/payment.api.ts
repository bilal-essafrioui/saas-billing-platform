import client from "../../../service/http/client";
import type { PaymentMethod } from "../types/payment.types";

export const getMyPaymentMethod = async (): Promise<PaymentMethod> => {
  const response = await client.get<PaymentMethod>(
    "/payments/method"
  );

  return response.data;
};