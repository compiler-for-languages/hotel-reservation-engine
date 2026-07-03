import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import toast from "react-hot-toast";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PaymentService } from "@/services/PaymentService";
import type { PaymentStatus } from "@/types/api";
import { formatCurrency, formatDateTime } from "@/utils/format";
import { getApiErrorMessage } from "@/utils/http";

export default function PaymentsAdminPage() {
  const queryClient = useQueryClient();
  const [pendingStartPaymentId, setPendingStartPaymentId] = useState<number | null>(null);
  const [pendingFailPaymentId, setPendingFailPaymentId] = useState<number | null>(null);
  const [pendingRefundPaymentId, setPendingRefundPaymentId] = useState<number | null>(null);
  const [pendingDeletePaymentId, setPendingDeletePaymentId] = useState<number | null>(null);
  const [lookupPaymentId, setLookupPaymentId] = useState("");
  const [statusFilter, setStatusFilter] = useState<PaymentStatus>("PENDING");

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["adminPayments"],
    queryFn: PaymentService.getAllPayments,
  });

  const startMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.startPayment(paymentId),
    onSuccess: () => {
      toast.success("Payment moved to processing.");
      void queryClient.invalidateQueries({ queryKey: ["adminPayments"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to update payment.")),
  });

  const failMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.markPaymentFailed(paymentId),
    onSuccess: () => {
      toast.success("Payment marked as failed.");
      void queryClient.invalidateQueries({ queryKey: ["adminPayments"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to update payment.")),
  });

  const refundMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.refundPayment(paymentId),
    onSuccess: () => {
      toast.success("Payment refunded successfully.");
      void queryClient.invalidateQueries({ queryKey: ["adminPayments"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to refund payment.")),
  });

  const byStatusMutation = useMutation({
    mutationFn: (status: PaymentStatus) => PaymentService.getPaymentsByStatus(status),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fetch payments by status.")),
  });

  const byIdMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.getPaymentById(paymentId),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fetch payment by ID.")),
  });

  const deleteMutation = useMutation({
    mutationFn: (paymentId: number) => PaymentService.deletePayment(paymentId),
    onSuccess: () => {
      toast.success("Payment deleted successfully.");
      void queryClient.invalidateQueries({ queryKey: ["adminPayments"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to delete payment.")),
  });

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-semibold text-slate-900">Payments</h2>

      <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4">
        <select className="rounded-md border border-slate-300 px-3 py-2 text-sm" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as PaymentStatus)}>
          <option value="PENDING">PENDING</option>
          <option value="PROCESSING">PROCESSING</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="FAILED">FAILED</option>
          <option value="REFUNDED">REFUNDED</option>
        </select>
        <button type="button" onClick={() => byStatusMutation.mutate(statusFilter)} className="rounded-md border border-slate-300 px-3 py-2 text-sm">
          Get By Status
        </button>
        <input value={lookupPaymentId} onChange={(event) => setLookupPaymentId(event.target.value)} type="number" placeholder="Payment ID" className="rounded-md border border-slate-300 px-3 py-2 text-sm" />
        <button
          type="button"
          onClick={() => {
            if (!lookupPaymentId) {
              toast.error("Payment ID is required.");
              return;
            }
            byIdMutation.mutate(Number(lookupPaymentId));
          }}
          className="rounded-md bg-slate-900 px-3 py-2 text-sm text-white"
        >
          Get By ID
        </button>
      </div>

      {byIdMutation.data ? (
        <div className="rounded-lg border border-slate-200 bg-white p-4 text-sm shadow-sm">
          <p>ID: {byIdMutation.data.paymentId}</p>
          <p>Reservation: {byIdMutation.data.reservationId}</p>
          <p>Status: {byIdMutation.data.paymentStatus}</p>
          <p>Amount: {formatCurrency(byIdMutation.data.amount, byIdMutation.data.currency)}</p>
        </div>
      ) : null}

      {byStatusMutation.data ? (
        <DataTable
          title="Payments By Status"
          data={byStatusMutation.data}
          rowKey={(row) => `status-${row.paymentId}`}
          searchTextExtractor={(row) => `${row.paymentId} ${row.reservationId}`}
          emptyMessage="No payments found for selected status."
          columns={[
            { key: "id", header: "Payment ID", render: (row) => row.paymentId },
            { key: "reservation", header: "Reservation", render: (row) => row.reservationId },
            { key: "status", header: "Status", render: (row) => <StatusBadge value={row.paymentStatus} /> },
          ]}
        />
      ) : null}

      <DataTable
        title="Payment Management"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load payments." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No payments available yet."
        rowKey={(row) => row.paymentId}
        searchPlaceholder="Search by payment ID, reservation, status"
        searchTextExtractor={(row) => `${row.paymentId} ${row.reservationId} ${row.paymentStatus}`}
        columns={[
          { key: "id", header: "Payment ID", render: (row) => row.paymentId, sortValue: (row) => row.paymentId },
          { key: "reservation", header: "Reservation", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "amount", header: "Amount", render: (row) => formatCurrency(row.amount, row.currency), sortValue: (row) => row.amount },
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
                <button
                  type="button"
                  className="rounded border border-slate-300 px-2 py-1 text-xs"
                  onClick={() => setPendingStartPaymentId(row.paymentId)}
                  disabled={startMutation.isPending || failMutation.isPending || refundMutation.isPending}
                >
                  Start
                </button>
                <button
                  type="button"
                  className="rounded border border-rose-200 px-2 py-1 text-xs text-rose-700"
                  onClick={() => setPendingFailPaymentId(row.paymentId)}
                  disabled={startMutation.isPending || failMutation.isPending || refundMutation.isPending}
                >
                  Fail
                </button>
                <button
                  type="button"
                  className="rounded border border-violet-200 px-2 py-1 text-xs text-violet-700"
                  onClick={() => setPendingRefundPaymentId(row.paymentId)}
                  disabled={startMutation.isPending || failMutation.isPending || refundMutation.isPending}
                >
                  Refund
                </button>
                <button
                  type="button"
                  className="rounded border border-rose-200 px-2 py-1 text-xs text-rose-700"
                  onClick={() => setPendingDeletePaymentId(row.paymentId)}
                  disabled={deleteMutation.isPending}
                >
                  Delete
                </button>
              </div>
            ),
          },
        ]}
      />

      <ConfirmDialog
        open={Boolean(pendingStartPaymentId)}
        title="Start Payment"
        description="Mark this payment as processing?"
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
        title="Mark Payment Failed"
        description="This will mark the payment as FAILED. Continue?"
        confirmLabel="Mark Failed"
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

      <ConfirmDialog
        open={Boolean(pendingRefundPaymentId)}
        title="Refund Payment"
        description="Refund this payment now? This action changes payment status to REFUNDED."
        confirmLabel="Refund"
        isConfirming={refundMutation.isPending}
        onCancel={() => setPendingRefundPaymentId(null)}
        onConfirm={() => {
          if (pendingRefundPaymentId) {
            refundMutation.mutate(pendingRefundPaymentId, {
              onSettled: () => setPendingRefundPaymentId(null),
            });
          }
        }}
      />

      <ConfirmDialog
        open={Boolean(pendingDeletePaymentId)}
        title="Delete Payment"
        description="This will permanently delete the selected payment record."
        confirmLabel="Delete"
        isConfirming={deleteMutation.isPending}
        onCancel={() => setPendingDeletePaymentId(null)}
        onConfirm={() => {
          if (pendingDeletePaymentId) {
            deleteMutation.mutate(pendingDeletePaymentId, {
              onSettled: () => setPendingDeletePaymentId(null),
            });
          }
        }}
      />
    </div>
  );
}