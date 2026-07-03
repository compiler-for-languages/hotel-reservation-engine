import { apiClient } from "@/services/apiClient";
import type { GuestPatchRequestDTO, GuestRequestDTO, GuestResponseDTO } from "@/types/api";

export const GuestService = {
  createGuest: async (payload: GuestRequestDTO): Promise<GuestResponseDTO> => {
    const { data } = await apiClient.post<GuestResponseDTO>("/api/guest/save", payload);
    return data;
  },

  updateGuest: async (guestId: number, payload: GuestPatchRequestDTO): Promise<GuestResponseDTO> => {
    const { data } = await apiClient.patch<GuestResponseDTO>(`/api/guest/update/${guestId}`, payload);
    return data;
  },

  getGuestsByReservation: async (reservationId: number): Promise<GuestResponseDTO[]> => {
    const { data } = await apiClient.get<GuestResponseDTO[]>("/api/guest/getbyreservation", { params: { reservationId } });
    return data;
  },

  getAllGuests: async (): Promise<GuestResponseDTO[]> => {
    const { data } = await apiClient.get<GuestResponseDTO[]>("/api/guest/getall");
    return data;
  },

  getGuestById: async (guestId: number): Promise<GuestResponseDTO> => {
    const { data } = await apiClient.get<GuestResponseDTO>(`/api/guest/get/${guestId}`);
    return data;
  },

  deleteGuest: async (guestId: number): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/api/guest/delete/${guestId}`);
    return data;
  },
};