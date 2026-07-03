import { useQuery } from "@tanstack/react-query";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import toast from "react-hot-toast";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { DataTable } from "@/components/common/DataTable";
import { Modal } from "@/components/common/Modal";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { ReservationService } from "@/services/ReservationService";
import { useAuthStore } from "@/store/authStore";
import { formatDate, formatDateTime, formatGuestNames } from "@/utils/format";
import { getApiErrorMessage } from "@/utils/http";
import { tableActionButtonClass, tableDangerButtonClass } from "@/utils/ui";

export default function MyReservationsPage() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [selectedReservationId, setSelectedReservationId] = useState<number | null>(null);
  const [deletingReservationId, setDeletingReservationId] = useState<number | null>(null);

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["myReservations", user?.userId],
    queryFn: () => ReservationService.getReservationsByUser(user!.userId),
    enabled: Boolean(user?.userId),
  });

  const reservationDetailMutation = useMutation({
    mutationFn: (reservationId: number) => ReservationService.getReservationById(reservationId),
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Unable to fetch reservation details."));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (reservationId: number) => ReservationService.deleteReservation(reservationId),
    onSuccess: () => {
      toast.success("Reservation cancelled successfully.");
      void queryClient.invalidateQueries({ queryKey: ["myReservations", user?.userId] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Unable to cancel reservation."));
    },
  });

  return (
    <div className="space-y-6">
      <PageHeader title="My Reservations" description="View and manage your booking requests." />

      <DataTable
        title="Reservations"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load reservations." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No reservations found yet."
        rowKey={(row) => row.reservationId}
        searchPlaceholder="Search by room type or status"
        searchTextExtractor={(row) =>
          `${row.roomTypeName} ${row.reservationStatus} ${row.reservationId} ${formatGuestNames(row.guests)}`
        }
        columns={[
          { key: "id", header: "Reservation ID", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "roomType", header: "Room Type", render: (row) => row.roomTypeName, sortValue: (row) => row.roomTypeName },
          { key: "checkIn", header: "Check In", render: (row) => formatDate(row.checkInDate), sortValue: (row) => row.checkInDate },
          { key: "checkOut", header: "Check Out", render: (row) => formatDate(row.checkOutDate), sortValue: (row) => row.checkOutDate },
          { key: "guestCount", header: "Guest Count", render: (row) => row.guestCount, sortValue: (row) => row.guestCount },
          {
            key: "guests",
            header: "Guests",
            render: (row) => formatGuestNames(row.guests),
            sortValue: (row) => formatGuestNames(row.guests),
          },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.reservationStatus} />, sortValue: (row) => row.reservationStatus },
          { key: "bookingTime", header: "Booked At", render: (row) => formatDateTime(row.bookingTime), sortValue: (row) => row.bookingTime },
          {
            key: "actions",
            header: "Actions",
            render: (row) => (
              <div className="flex gap-2">
                <button
                  type="button"
                  className={tableActionButtonClass}
                  onClick={() => {
                    setSelectedReservationId(row.reservationId);
                    reservationDetailMutation.mutate(row.reservationId);
                  }}
                >
                  View
                </button>
                <button type="button" className={tableDangerButtonClass} onClick={() => setDeletingReservationId(row.reservationId)}>
                  Cancel
                </button>
              </div>
            ),
          },
        ]}
      />

      <Modal open={Boolean(selectedReservationId)} title="Reservation Details" onClose={() => setSelectedReservationId(null)}>
        {reservationDetailMutation.isPending ? <p className="text-sm text-slate-600">Loading details...</p> : null}
        {reservationDetailMutation.data ? (
          <div className="space-y-1 text-sm text-slate-700">
            <p>Reservation ID: {reservationDetailMutation.data.reservationId}</p>
            <p>Customer: {reservationDetailMutation.data.userName}</p>
            <p>Room Type: {reservationDetailMutation.data.roomTypeName}</p>
            <p>Status: {reservationDetailMutation.data.reservationStatus}</p>
            <p>Check In: {formatDate(reservationDetailMutation.data.checkInDate)}</p>
            <p>Check Out: {formatDate(reservationDetailMutation.data.checkOutDate)}</p>
            <p>Guest Count: {reservationDetailMutation.data.guestCount}</p>
            <p>Guests: {formatGuestNames(reservationDetailMutation.data.guests)}</p>
            <p>Special Request: {reservationDetailMutation.data.specialRequest || "-"}</p>
          </div>
        ) : null}
      </Modal>

      <ConfirmDialog
        open={Boolean(deletingReservationId)}
        title="Cancel Reservation"
        description="Do you want to cancel this reservation?"
        confirmLabel="Cancel Reservation"
        isConfirming={deleteMutation.isPending}
        onCancel={() => setDeletingReservationId(null)}
        onConfirm={() => {
          if (deletingReservationId) {
            deleteMutation.mutate(deletingReservationId, {
              onSettled: () => setDeletingReservationId(null),
            });
          }
        }}
      />
    </div>
  );
}
