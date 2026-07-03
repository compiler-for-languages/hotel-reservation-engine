import { apiClient } from "@/services/apiClient";
import type {
  AvailabilityCustomerResponseDTO,
  AvailabilityRequestDTO,
  AvailabilityResponseDTO,
} from "@/types/api";

export const AvailabilityService = {
  searchAvailability: async (payload: AvailabilityRequestDTO): Promise<AvailabilityCustomerResponseDTO> => {
    const { data } = await apiClient.post<AvailabilityCustomerResponseDTO>("/api/availability/search", payload);
    return data;
  },

  checkAvailability: async (payload: AvailabilityRequestDTO): Promise<AvailabilityResponseDTO> => {
    const { data } = await apiClient.post<AvailabilityResponseDTO>("/api/availability/check", payload);
    return data;
  },
};