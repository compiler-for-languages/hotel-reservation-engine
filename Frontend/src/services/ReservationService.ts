import { apiClient } from "@/services/apiClient";
import type {
  ReservationPatchRequestDTO,
  ReservationRequestDTO,
  ReservationResponseDTO,
  ReservationStatus,
} from "@/types/api";

export const ReservationService = {
  createReservation: async (payload: ReservationRequestDTO): Promise<ReservationResponseDTO> => {
    const { data } = await apiClient.post<ReservationResponseDTO>("/api/reservation/save", payload);
    return data;
  },

  updateReservation: async (
    reservationId: number,
    payload: ReservationPatchRequestDTO
  ): Promise<ReservationResponseDTO> => {
    const { data } = await apiClient.patch<ReservationResponseDTO>(`/api/reservation/update/${reservationId}`, payload);
    return data;
  },

  getReservationsByUser: async (userId: number): Promise<ReservationResponseDTO[]> => {
    const { data } = await apiClient.get<ReservationResponseDTO[]>("/api/reservation/getbyuser", { params: { userId } });
    return data;
  },

  getReservationsByStatus: async (status: ReservationStatus): Promise<ReservationResponseDTO[]> => {
    const { data } = await apiClient.get<ReservationResponseDTO[]>("/api/reservation/getbystatus", { params: { status } });
    return data;
  },

  getAllReservations: async (): Promise<ReservationResponseDTO[]> => {
    const { data } = await apiClient.get<ReservationResponseDTO[]>("/api/reservation/getall");
    return data;
  },

  getReservationById: async (reservationId: number): Promise<ReservationResponseDTO> => {
    const { data } = await apiClient.get<ReservationResponseDTO>(`/api/reservation/get/${reservationId}`);
    return data;
  },

  deleteReservation: async (reservationId: number): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/api/reservation/delete/${reservationId}`);
    return data;
  },
};