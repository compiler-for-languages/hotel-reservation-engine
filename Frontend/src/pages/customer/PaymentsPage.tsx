import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import toast from "react-hot-toast";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { DataTable } from "@/components/common/DataTable";
import { FormErrorText } from "@/components/common/FormErrorText";
import { Modal } from "@/components/common/Modal";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PaymentService } from "@/services/PaymentService";
import { ReservationService } from "@/services/ReservationService";
import { useAuthStore } from "@/store/authStore";
import type { PaymentResponseDTO } from "@/types/api";
import { formatCurrency, formatDateTime } from "@/utils/format";
import { getApiErrorMessage } from "@/utils/http";

export default function PaymentsPage() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [paymentIdForSuccess, setPaymentIdForSuccess] = useState<number | null>(null);
  const [pendingStartPaymentId, setPendingStartPaymentId] = useState<number | null>(null);
  const [pendingFailPaymentId, setPendingFailPaymentId] = useState<number | null>(null);
  const [gatewayPaymentId, setGatewayPaymentId] = useState("");
  const [gatewaySignature, setGatewaySignature] = useState("");
  const [selectedPayment, setSelectedPayment] = useState<PaymentResponseDTO | null>(null);

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["customerPayments", user?.userId],
    enabled: Boolean(user?.userId),
    queryFn: async () => {
      const reservations = await ReservationService.getReservationsByUser(user!.userId);
      const paymentResults = await Promise.allSettled(
        reservations.map((reservation) => PaymentService.getPaymentByReservation(reservation.reservationId))
      );

      return paymentResults
        .filter((result): result is PromiseFulfilledResult<PaymentResponseDTO> => result.status === "fulfilled")
        .map((result) => result.value);
    },
  });

  const startMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.startPayment(paymentId),
    onSuccess: () => {
      toast.success("Payment moved to processing.");
      void queryClient.invalidateQueries({ queryKey: ["customerPayments", user?.userId] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to start payment.")),
  });

  const failMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.markPaymentFailed(paymentId),
    onSuccess: () => {
      toast.success("Payment marked as failed.");
      void queryClient.invalidateQueries({ queryKey: ["customerPayments", user?.userId] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fail payment.")),
  });

  const successMutation = useMutation({
    mutationFn: () => {
      if (!paymentIdForSuccess) {
        throw new Error("Select payment first.");
      }
      return PaymentService.markPaymentSuccess(paymentIdForSuccess, gatewayPaymentId, gatewaySignature);
    },
    onSuccess: () => {
      toast.success("Payment marked as successful.");
      setPaymentIdForSuccess(null);
      setGatewayPaymentId("");
      setGatewaySignature("");
      void queryClient.invalidateQueries({ queryKey: ["customerPayments", user?.userId] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to complete payment.")),
  });

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-semibold text-slate-900">Payments</h2>

      <DataTable
        title="Payment Records"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load payments." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No payments available yet."
        rowKey={(row) => row.paymentId}
        searchPlaceholder="Search by payment ID, reservation, or status"
        searchTextExtractor={(row) => `${row.paymentId} ${row.reservationId} ${row.paymentStatus}`}
        columns={[
          { key: "paymentId", header: "Payment ID", render: (row) => row.paymentId, sortValue: (row) => row.paymentId },
          {
            key: "reservationId",
            header: "Reservation ID",
            render: (row) => row.reservationId,
            sortValue: (row) => row.reservationId,
          },
          {
            key: "amount",
            header: "Amount",
            render: (row) => formatCurrency(row.amount, row.currency || "INR"),
            sortValue: (row) => row.amount,
          },
          { key: "method", header: "Method", render: (row) => row.paymentMethod, sortValue: (row) => row.paymentMethod },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.paymentStatus} />, sortValue: (row) => row.paymentStatus },
          {
            key: "paidAt",
            header: "Paid At",
            render: (row) => (row.paidAt ? formatDateTime(row.paidAt) : "-"),
            sortValue: (row) => row.paidAt || "",
          },
          {
            key: "actions",
            header: "Actions",
            render: (row) => (
              <div className="flex gap-2">
                <button type="button" className="rounded border border-slate-300 px-2 py-1 text-xs" onClick={() => setPendingStartPaymentId(row.paymentId)}>
                  Start
                </button>
                <button
                  type="button"
                  className="rounded border border-emerald-200 px-2 py-1 text-xs text-emerald-700"
                  onClick={() => setPaymentIdForSuccess(row.paymentId)}
                >
                  Success
                </button>
                <button
                  type="button"
                  className="rounded border border-slate-300 px-2 py-1 text-xs"
                  onClick={() => {
                    setSelectedPayment(row);
                  }}
                >
                  View
                </button>
                <button type="button" className="rounded border border-rose-200 px-2 py-1 text-xs text-rose-700" onClick={() => setPendingFailPaymentId(row.paymentId)}>
                  Fail
                </button>
              </div>
            ),
          },
        ]}
      />

      {paymentIdForSuccess ? (
        <div className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="text-sm font-semibold text-slate-900">Complete Payment #{paymentIdForSuccess}</h3>
          <div className="grid gap-3 md:grid-cols-2">
            <input
              value={gatewayPaymentId}
              onChange={(event) => setGatewayPaymentId(event.target.value)}
              placeholder="gatewayPaymentId"
              className="rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
            <input
              value={gatewaySignature}
              onChange={(event) => setGatewaySignature(event.target.value)}
              placeholder="gatewaySignature"
              className="rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div className="grid gap-1 md:grid-cols-2">
            <FormErrorText message={!gatewayPaymentId ? "gatewayPaymentId is required." : undefined} />
            <FormErrorText message={!gatewaySignature ? "gatewaySignature is required." : undefined} />
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => successMutation.mutate()}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
              disabled={successMutation.isPending || !gatewayPaymentId || !gatewaySignature}
            >
              {successMutation.isPending ? "Submitting..." : "Confirm Success"}
            </button>
            <button
              type="button"
              onClick={() => setPaymentIdForSuccess(null)}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-700"
            >
              Cancel
            </button>
          </div>
        </div>
      ) : null}

      <Modal open={Boolean(selectedPayment)} title="Payment Details" onClose={() => setSelectedPayment(null)}>
        {selectedPayment ? (
          <div className="space-y-1 text-sm text-slate-700">
            <p>Payment ID: {selectedPayment.paymentId}</p>
            <p>Reservation ID: {selectedPayment.reservationId}</p>
            <p>Amount: {formatCurrency(selectedPayment.amount, selectedPayment.currency)}</p>
            <p>Method: {selectedPayment.paymentMethod}</p>
            <p>Status: {selectedPayment.paymentStatus}</p>
            <p>Gateway Order ID: {selectedPayment.gatewayOrderId || "-"}</p>
            <p>Gateway Payment ID: {selectedPayment.gatewayPaymentId || "-"}</p>
            <p>Paid At: {selectedPayment.paidAt ? formatDateTime(selectedPayment.paidAt) : "-"}</p>
          </div>
        ) : null}
      </Modal>

      <ConfirmDialog
        open={Boolean(pendingStartPaymentId)}
        title="Start Payment"
        description="Move this payment to PROCESSING state?"
        confirmLabel="Start"
        isConfirming={startMutation.isPending}
        onCancel={() => setPendingStartPaymentId(null)}
        onConfirm={() => {
          if (pendingStartPaymentId) {
            startMutation.mutate(pendingStartPaymentId, {
              onSettled: () => setPendingStartPaymentId(null),
            });
          }
        }}
      />

      <ConfirmDialog
        open={Boolean(pendingFailPaymentId)}
        title="Fail Payment"
        description="Mark this payment as FAILED?"
        confirmLabel="Fail"
        isConfirming={failMutation.isPending}
        onCancel={() => setPendingFailPaymentId(null)}
        onConfirm={() => {
          if (pendingFailPaymentId) {
            failMutation.mutate(pendingFailPaymentId, {
              onSettled: () => setPendingFailPaymentId(null),
            });
          }
        }}
      />
    </div>
  );
}