import { useMutation } from "@tanstack/react-query";
import toast from "react-hot-toast";
import { AuthService } from "@/services/AuthService";
import { useAuthStore } from "@/store/authStore";
import type { LoginRequestDTO, RegisterRequestDTO } from "@/types/api";
import { getUserFromToken } from "@/utils/jwt";
import { storage } from "@/utils/storage";

export const useLogin = () => {
  const setSession = useAuthStore((state) => state.setSession);

  return useMutation({
    mutationFn: async (payload: LoginRequestDTO) => {
      const response = await AuthService.login(payload);
      storage.setToken(response.token);

      try {
        const user = await AuthService.getCurrentUser();
        setSession({ token: response.token, user });
        return user;
      } catch {
        const user = getUserFromToken(response.token);
        if (!user) {
          throw new Error("Invalid token received from server.");
        }
        setSession({ token: response.token, user });
        return user;
      }
    },
  });
};

export const useRegister = () =>
  useMutation({
    mutationFn: async (payload: RegisterRequestDTO) => AuthService.registerCustomer(payload),
    onSuccess: () => {
      toast.success("Registration successful. Please login.");
    },
  });
