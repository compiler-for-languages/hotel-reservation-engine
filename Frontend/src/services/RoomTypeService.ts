import { apiClient } from "@/services/apiClient";
import type { RoomTypeRequestDTO, RoomTypeResponseDTO } from "@/types/api";

export const RoomTypeService = {
  createRoomType: async (payload: RoomTypeRequestDTO): Promise<RoomTypeResponseDTO> => {
    const { data } = await apiClient.post<RoomTypeResponseDTO>("/api/admin/roomtype/save", payload);
    return data;
  },

  updateRoomType: async (roomTypeId: number, payload: RoomTypeRequestDTO): Promise<RoomTypeResponseDTO> => {
    const { data } = await apiClient.patch<RoomTypeResponseDTO>(`/api/admin/roomtype/update/${roomTypeId}`, payload);
    return data;
  },

  getAllRoomTypes: async (): Promise<RoomTypeResponseDTO[]> => {
    const { data } = await apiClient.get<RoomTypeResponseDTO[]>("/api/admin/roomtype/getall");
    return data;
  },

  getRoomTypeByName: async (name: string): Promise<RoomTypeResponseDTO> => {
    const { data } = await apiClient.get<RoomTypeResponseDTO>("/api/admin/roomtype/get", { params: { name } });
    return data;
  },

  getRoomTypeById: async (roomTypeId: number): Promise<RoomTypeResponseDTO> => {
    const { data } = await apiClient.get<RoomTypeResponseDTO>(`/api/admin/roomtype/get/${roomTypeId}`);
    return data;
  },

  deleteRoomType: async (roomTypeId: number): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/api/admin/roomtype/delete/${roomTypeId}`);
    return data;
  },
};