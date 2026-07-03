import { AuthService } from "@/services/AuthService";
import { useAuthStore } from "@/store/authStore";
import { getUserFromToken } from "@/utils/jwt";
import { clearApplicationSession } from "@/utils/session";
import { storage } from "@/utils/storage";
import { useEffect } from "react";

export const useAuthBootstrap = () => {
  const isAuthInitializing = useAuthStore((state) => state.isAuthInitializing);
  const setAuthInitializing = useAuthStore((state) => state.setAuthInitializing);
  const setSession = useAuthStore((state) => state.setSession);

  useEffect(() => {
    let mounted = true;

    const initialize = async () => {
      const token = storage.getToken();

      if (!token) {
        if (mounted) {
          setAuthInitializing(false);
        }
        return;
      }

      const tokenUser = getUserFromToken(token);
      if (!tokenUser) {
        clearApplicationSession();
        if (mounted) {
          setAuthInitializing(false);
        }
        return;
      }

      try {
        const user = await AuthService.getCurrentUser();
        if (mounted) {
          setSession({ token, user });
        }
      } catch {
        if (mounted) {
          setSession({ token, user: tokenUser });
        }
      } finally {
        if (mounted) {
          setAuthInitializing(false);
        }
      }
    };

    void initialize();

    return () => {
      mounted = false;
    };
  }, [setAuthInitializing, setSession]);

  return { isAuthInitializing };
};
