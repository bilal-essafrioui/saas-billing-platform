import client from "../../../service/http/client";
import type { CheckoutResponse, SubscriptionResponse, SubscriptionEventResponse } from "../types/subscription.types";
import type {  } from "../types/subscription.types";

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


export async function getMySubscription(): Promise<SubscriptionResponse> {
  const response = await client.get<SubscriptionResponse>(
    "/subscriptions/me"
  );

  return response.data;
}



export async function getMySubscriptionHistory(): Promise<SubscriptionEventResponse[]> {
  const response = await client.get<SubscriptionEventResponse[]>(
    "/subscriptions/me/history"
  );

  return response.data;
}