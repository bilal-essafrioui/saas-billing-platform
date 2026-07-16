import { useEffect, useState } from "react";
import { getPlans } from "../api/subscription.api";

type Plan = {
  id: string;
  name: string;
  price: number;
  description: string;
  active: boolean;
};

export function usePlans() {
  const [plans, setPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPlans = async () => {
      try {
        const data = await getPlans();
        setPlans(data);
      } catch (err) {
        setError("Failed to load plans.");
      } finally {
        setLoading(false);
      }
    };

    fetchPlans();
  }, []);

  return {
    plans,
    loading,
    error,
  };
}