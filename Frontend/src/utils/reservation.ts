import type { ReservationStatus } from "@/types/api";

/** Valid admin manual status transitions aligned with business workflow. */
export const ADMIN_RESERVATION_TRANSITIONS: Record<ReservationStatus, ReservationStatus[]> = {
  PENDING: ["CONFIRMED", "CANCELLED", "EXPIRED"],
  CONFIRMED: ["CANCELLED"],
  CHECKED_IN: [],
  CHECKED_OUT: [],
  CANCELLED: [],
  EXPIRED: [],
};

export const getAllowedReservationTransitions = (status: ReservationStatus): ReservationStatus[] =>
  ADMIN_RESERVATION_TRANSITIONS[status] ?? [];
