import { apiClient } from "@/services/apiClient";
import type {
  AssignRoomRequestDTO,
  ReceptionDashboardResponseDTO,
  RoomAssignmentResponseDTO,
  TodayArrivalResponseDTO,
  TodayDepartureResponseDTO,
} from "@/types/api";

export const ReceptionService = {
  assignRoom: async (payload: AssignRoomRequestDTO): Promise<RoomAssignmentResponseDTO> => {
    const { data } = await apiClient.post<RoomAssignmentResponseDTO>("/api/reception/assign-room", payload);
    return data;
  },

  checkOut: async (payload: AssignRoomRequestDTO): Promise<RoomAssignmentResponseDTO> => {
    const { data } = await apiClient.patch<RoomAssignmentResponseDTO>("/api/reception/check-out", payload);
    return data;
  },

  checkIn: async (payload: AssignRoomRequestDTO): Promise<RoomAssignmentResponseDTO> => {
    const { data } = await apiClient.patch<RoomAssignmentResponseDTO>("/api/reception/check-in", payload);
    return data;
  },

  getTodayDepartures: async (): Promise<TodayDepartureResponseDTO[]> => {
    const { data } = await apiClient.get<TodayDepartureResponseDTO[]>("/api/reception/today-departures");
    return data;
  },

  getTodayArrivals: async (): Promise<TodayArrivalResponseDTO[]> => {
    const { data } = await apiClient.get<TodayArrivalResponseDTO[]>("/api/reception/today-arrivals");
    return data;
  },

  getDashboard: async (): Promise<ReceptionDashboardResponseDTO> => {
    const { data } = await apiClient.get<ReceptionDashboardResponseDTO>("/api/reception/dashboard");
    return data;
  },
};
