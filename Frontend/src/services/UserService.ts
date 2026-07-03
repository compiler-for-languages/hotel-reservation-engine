import { apiClient } from "@/services/apiClient";
import type { UserPatchRequestDTO, UserRequestDTO, UserResponseDTO } from "@/types/api";

export const UserService = {
  createReceptionist: async (payload: UserRequestDTO): Promise<UserResponseDTO> => {
    const { data } = await apiClient.post<UserResponseDTO>("/api/admin/users/receptionist", payload);
    return data;
  },

  updateUser: async (userId: number, payload: UserPatchRequestDTO): Promise<UserResponseDTO> => {
    const { data } = await apiClient.patch<UserResponseDTO>(`/api/admin/users/update/${userId}`, payload);
    return data;
  },

  getAllReceptionists: async (): Promise<UserResponseDTO[]> => {
    const { data } = await apiClient.get<UserResponseDTO[]>("/api/admin/users/receptionists");
    return data;
  },

  getAllUsers: async (): Promise<UserResponseDTO[]> => {
    const { data } = await apiClient.get<UserResponseDTO[]>("/api/admin/users/getall");
    return data;
  },

  getUserByEmail: async (email: string): Promise<UserResponseDTO> => {
    const { data } = await apiClient.get<UserResponseDTO>("/api/admin/users/get", { params: { email } });
    return data;
  },

  getUserById: async (userId: number): Promise<UserResponseDTO> => {
    const { data } = await apiClient.get<UserResponseDTO>(`/api/admin/users/get/${userId}`);
    return data;
  },

  getAllCustomers: async (): Promise<UserResponseDTO[]> => {
    const { data } = await apiClient.get<UserResponseDTO[]>("/api/admin/users/customers");
    return data;
  },

  deleteUser: async (userId: number): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/api/admin/users/delete/${userId}`);
    return data;
  },
};