import client from "../../../service/http/client";
import type { CheckoutResponse } from "../types/subscription.types";

export const getPlans = async () => {
  const response = await client.get("/subscriptions/plans");
  return response.data;
};

export async function checkout(planId: string) {
  const { data } = await client.post<CheckoutResponse>(
    "/subscriptions/checkout",
    {
      planId,
    }
  );

  return data;
}