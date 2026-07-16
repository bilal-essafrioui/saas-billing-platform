import client from "../../../service/http/client";
import type { RegisterRequest, RegisterResponse } from "../types/auth.types";
import type {
  LoginRequest,
  LoginResponse,
} from "../types/auth.types";

export async function login(
  request: LoginRequest
): Promise<LoginResponse> {
  const response = await client.post<LoginResponse>(
    "/auth/login",
    request
  );

  return response.data;
}

// Register
export async function register(
  data: RegisterRequest
): Promise<RegisterResponse> {

  const response = await client.post<RegisterResponse>(
    "/auth/register",
    data
  );

  return response.data;
}

