import { jwtDecode } from "jwt-decode";
import type { UserResponseDTO, Role } from "@/types/api";

interface CustomJwtPayload {
  sub?: string;
  userId?: number;
  role?: Role;
  firstName?: string;
  lastName?: string;
  phone?: string;
  exp?: number;
  iat?: number;
}

export const decodeEmailFromToken = (token: string): string | null => {
  try {
    const payload = jwtDecode<CustomJwtPayload>(token);
    return payload.sub ?? null;
  } catch {
    return null;
  }
};

export const getUserFromToken = (token: string): UserResponseDTO | null => {
  try {
    const payload = jwtDecode<CustomJwtPayload>(token);
    if (!payload.sub || !payload.userId || !payload.role) {
      return null;
    }
    return {
      userId: payload.userId,
      firstName: payload.firstName ?? "",
      lastName: payload.lastName ?? "",
      email: payload.sub,
      phone: payload.phone ?? "",
      role: payload.role,
      accountStatus: "ACTIVE",
    };
  } catch {
    return null;
  }
};