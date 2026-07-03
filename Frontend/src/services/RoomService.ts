import { apiClient } from "@/services/apiClient";
import type { RoomPatchRequestDTO, RoomRequestDTO, RoomResponseDTO, RoomStatus } from "@/types/api";

export const RoomService = {
  createRoom: async (payload: RoomRequestDTO): Promise<RoomResponseDTO> => {
    const { data } = await apiClient.post<RoomResponseDTO>("/api/admin/room/save", payload);
    return data;
  },

  updateRoom: async (roomId: number, payload: RoomPatchRequestDTO): Promise<RoomResponseDTO> => {
    const { data } = await apiClient.patch<RoomResponseDTO>(`/api/admin/room/update/${roomId}`, payload);
    return data;
  },

  getRoomsByRoomType: async (roomTypeId: number): Promise<RoomResponseDTO[]> => {
    const { data } = await apiClient.get<RoomResponseDTO[]>("/api/admin/room/getbyroomtype", { params: { roomTypeId } });
    return data;
  },

  getAllRooms: async (): Promise<RoomResponseDTO[]> => {
    const { data } = await apiClient.get<RoomResponseDTO[]>("/api/admin/room/getall");
    return data;
  },

  getRoomByRoomNumber: async (roomNumber: string): Promise<RoomResponseDTO> => {
    const { data } = await apiClient.get<RoomResponseDTO>("/api/admin/room/get", { params: { roomNumber } });
    return data;
  },

  getRoomById: async (roomId: number): Promise<RoomResponseDTO> => {
    const { data } = await apiClient.get<RoomResponseDTO>(`/api/admin/room/get/${roomId}`);
    return data;
  },

  getRoomsByRoomTypeAndStatus: async (roomTypeId: number, roomStatus: RoomStatus): Promise<RoomResponseDTO[]> => {
    const { data } = await apiClient.get<RoomResponseDTO[]>("/api/admin/room/filter", { params: { roomTypeId, roomStatus } });
    return data;
  },

  deleteRoom: async (roomId: number): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/api/admin/room/delete/${roomId}`);
    return data;
  },
};