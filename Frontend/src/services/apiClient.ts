import axios from "axios";
import toast from "react-hot-toast";
import { useAuthStore } from "@/store/authStore";
import { getDashboardPathForCurrentUser } from "@/utils/session";
import { clearApplicationSession } from "@/utils/session";
import { storage } from "@/utils/storage";

const baseURL = import.meta.env.VITE_API_BASE_URL;

let isHandlingAuthError = false;

export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token ?? storage.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status as number | undefined;

    if ((status === 401 || status === 403) && !isHandlingAuthError) {
      isHandlingAuthError = true;

      if (status === 401) {
        clearApplicationSession();
        toast.error("Session expired. Please login again.");
        window.location.href = "/login";
      }

      if (status === 403) {
        toast.error("You are not authorized to access this page.");
        const isAuthenticated = useAuthStore.getState().isAuthenticated;
        window.location.href = isAuthenticated ? getDashboardPathForCurrentUser() : "/login";
      }

      window.setTimeout(() => {
        isHandlingAuthError = false;
      }, 500);
    }

    return Promise.reject(error);
  }
);