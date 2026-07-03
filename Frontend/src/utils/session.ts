import { useAuthStore } from "@/store/authStore";
import { useUiStore } from "@/store/uiStore";
import { dashboardByRole } from "@/utils/role";
import { storage } from "@/utils/storage";

export const clearApplicationSession = () => {
  storage.clearToken();
  useAuthStore.getState().clearSession();
  useUiStore.getState().resetUi();
};

export const getDashboardPathForCurrentUser = () => {
  const role = useAuthStore.getState().user?.role;
  return role ? dashboardByRole[role] : "/login";
};