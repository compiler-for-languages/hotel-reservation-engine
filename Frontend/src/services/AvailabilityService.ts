import { apiClient } from "@/services/apiClient";
import type {
  AvailabilityCustomerResponseDTO,
  AvailabilityRequestDTO,
} from "@/types/api";

export const AvailabilityService = {
  searchAvailability: async (payload: AvailabilityRequestDTO): Promise<AvailabilityCustomerResponseDTO> => {
    const { data } = await apiClient.post<AvailabilityCustomerResponseDTO>("/api/availability/search", payload);
    return data;
  },
};