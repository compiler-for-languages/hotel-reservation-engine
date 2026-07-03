import { apiClient } from "@/services/apiClient";
import type { PaymentRequestDTO, PaymentResponseDTO, PaymentStatus } from "@/types/api";

export const PaymentService = {
  createPayment: async (payload: PaymentRequestDTO): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.post<PaymentResponseDTO>("/api/payment/save", payload);
    return data;
  },

  markPaymentSuccess: async (
    paymentId: number,
    gatewayPaymentId: string,
    gatewaySignature: string
  ): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.patch<PaymentResponseDTO>(`/api/payment/success/${paymentId}`, null, {
      params: { gatewayPaymentId, gatewaySignature },
    });
    return data;
  },

  startPayment: async (paymentId: number): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.patch<PaymentResponseDTO>(`/api/payment/start/${paymentId}`);
    return data;
  },

  refundPayment: async (paymentId: number): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.patch<PaymentResponseDTO>(`/api/payment/refund/${paymentId}`);
    return data;
  },

  markPaymentFailed: async (paymentId: number): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.patch<PaymentResponseDTO>(`/api/payment/fail/${paymentId}`);
    return data;
  },

  getPaymentsByStatus: async (paymentStatus: PaymentStatus): Promise<PaymentResponseDTO[]> => {
    const { data } = await apiClient.get<PaymentResponseDTO[]>("/api/payment/status", { params: { paymentStatus } });
    return data;
  },

  getPaymentByReservation: async (reservationId: number): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.get<PaymentResponseDTO>(`/api/payment/reservation/${reservationId}`);
    return data;
  },

  getAllPayments: async (): Promise<PaymentResponseDTO[]> => {
    const { data } = await apiClient.get<PaymentResponseDTO[]>("/api/payment/getall");
    return data;
  },

  getPaymentById: async (paymentId: number): Promise<PaymentResponseDTO> => {
    const { data } = await apiClient.get<PaymentResponseDTO>(`/api/payment/get/${paymentId}`);
    return data;
  },

  deletePayment: async (paymentId: number): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/api/payment/delete/${paymentId}`);
    return data;
  },
};