import client from "../../../service/http/client";

export const getPlans = async () => {
  const response = await client.get("/subscriptions/plans");
  return response.data;
};