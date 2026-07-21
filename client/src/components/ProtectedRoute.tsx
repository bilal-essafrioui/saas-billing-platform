// Dans ProtectedRoute
import { Navigate, Outlet } from "react-router-dom";
import Spinner from "../components/ui/Spinner";
import useAuth from "../app/hooks/useAuth";
import { toast } from "sonner";
import { useEffect } from "react";

export default function ProtectedRoute() {
  const { user, loading } = useAuth();

  useEffect(() => {
    if (!loading && !user) {
      toast.error("Unauthorized. Please log in first.");
    }
  }, [loading, user]);

  if (loading) {
    return <Spinner />;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}