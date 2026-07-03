import { apiClient } from "@/services/apiClient";
import type { LoginRequestDTO, LoginResponseDTO, RegisterRequestDTO, UserResponseDTO } from "@/types/api";

export const AuthService = {
  registerCustomer: async (payload: RegisterRequestDTO): Promise<UserResponseDTO> => {
    const { data } = await apiClient.post<UserResponseDTO>("/api/auth/register", payload);
    return data;
  },

  login: async (payload: LoginRequestDTO): Promise<LoginResponseDTO> => {
    const { data } = await apiClient.post<LoginResponseDTO>("/api/auth/login", payload);
    return data;
  },

  getCurrentUser: async (): Promise<UserResponseDTO> => {
    const { data } = await apiClient.get<UserResponseDTO>("/api/auth/me");
    return data;
  },
};