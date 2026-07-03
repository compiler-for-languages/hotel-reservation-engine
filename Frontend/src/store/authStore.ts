import { create } from "zustand";
import type { UserResponseDTO } from "@/types/api";

interface AuthState {
  token: string | null;
  user: UserResponseDTO | null;
  isAuthenticated: boolean;
  isAuthInitializing: boolean;
  setAuthInitializing: (value: boolean) => void;
  setSession: (params: { token: string; user: UserResponseDTO }) => void;
  clearSession: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,
  isAuthenticated: false,
  isAuthInitializing: true,
  setAuthInitializing: (value) => set({ isAuthInitializing: value }),
  setSession: ({ token, user }) =>
    set({
      token,
      user,
      isAuthenticated: true,
      isAuthInitializing: false,
    }),
  clearSession: () =>
    set({
      token: null,
      user: null,
      isAuthenticated: false,
      isAuthInitializing: false,
    }),
}));