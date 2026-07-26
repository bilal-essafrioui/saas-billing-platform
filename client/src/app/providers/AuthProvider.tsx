import { useEffect, useState, useCallback } from "react";
import type { ReactNode } from "react";
import { AuthContext } from "../contexts/AuthContext";
import { me, logout as logoutApi  } from "../../features/auth/api/auth.api";
import type { User } from "../../features/auth/types/auth.types";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

type Props = {
  children: ReactNode;
};

export default function AuthProvider({ children }: Props) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const checkAuth = useCallback(async () => {
    try {
      const currentUser = await me();

      setUser(currentUser);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutApi();
      setUser(null);
      navigate("/");
      toast.success("Logged out successfully!");
    } catch {
      toast.error("Failed to log out.");
    }
  }, [navigate]);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        checkAuth,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}