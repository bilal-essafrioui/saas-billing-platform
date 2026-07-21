import { createContext } from "react";
import type { User } from "../../features/auth/types/auth.types";

export type AuthContextType = {
  user: User | null;
  loading: boolean;

  checkAuth: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);