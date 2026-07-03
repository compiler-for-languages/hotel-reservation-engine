import type { Role } from "@/types/api";

export const dashboardByRole: Record<Role, string> = {
  CUSTOMER: "/customer/dashboard",
  RECEPTIONIST: "/receptionist/dashboard",
  ADMIN: "/admin/dashboard",
};

export const roleLabel: Record<Role, string> = {
  CUSTOMER: "Customer",
  RECEPTIONIST: "Receptionist",
  ADMIN: "Admin",
};