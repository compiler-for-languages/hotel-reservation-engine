import { apiClient } from "@/services/apiClient";
import type { GuestRequestDTO, GuestResponseDTO } from "@/types/api";

export const GuestService = {
  createGuest: async (payload: GuestRequestDTO): Promise<GuestResponseDTO> => {
    const { data } = await apiClient.post<GuestResponseDTO>("/api/guest/save", payload);
    return data;
  },
};
