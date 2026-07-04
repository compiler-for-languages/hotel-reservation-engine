import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import toast from "react-hot-toast";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { DataTable } from "@/components/common/DataTable";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { ReservationService } from "@/services/ReservationService";
import type { ReservationStatus } from "@/types/api";
import { formatDate, formatGuestNames } from "@/utils/format";
import { getApiErrorMessage } from "@/utils/http";
import { primaryButtonClass, secondaryButtonClass, tableDangerButtonClass } from "@/utils/ui";

export default function ReservationsPage() {
  const queryClient = useQueryClient();
  const [pendingCancelReservationId, setPendingCancelReservationId] = useState<number | null>(null);
  const [lookupReservationId, setLookupReservationId] = useState("");
  const [statusFilter, setStatusFilter] = useState<ReservationStatus>("PENDING");

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["adminReservations"],
    queryFn: ReservationService.getAllReservations,
  });

  const cancelMutation = useMutation({
    mutationFn: (reservationId: number) => {
      const reservation = data.find((item) => item.reservationId === reservationId);
      if (!reservation) {
        throw new Error("Reservation not found.");
      }

      return ReservationService.updateReservation(reservationId, {
        reservationStatus: "CANCELLED",
        specialRequest: reservation.specialRequest,
        guestCount: reservation.guestCount,
      });
    },
    onSuccess: () => {
      toast.success("Reservation cancelled successfully.");
      void queryClient.invalidateQueries({ queryKey: ["adminReservations"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to cancel reservation."));
    },
  });

  const byStatusMutation = useMutation({
    mutationFn: (status: ReservationStatus) => ReservationService.getReservationsByStatus(status),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fetch reservations by status.")),
  });

  const byIdMutation = useMutation({
    mutationFn: (reservationId: number) => ReservationService.getReservationById(reservationId),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fetch reservation by ID.")),
  });


  return (
    <div className="space-y-6">
      <PageHeader
        title="Reservations"
        description="View and manage reservations. Status transitions are customer-driven. Check-in, check-out, and room assignment are handled by reception."
      />

      <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4">
        <select className="rounded-md border border-slate-300 px-3 py-2 text-sm" title="Filter reservations by status" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as ReservationStatus)}>
          <option value="PENDING">PENDING</option>
          <option value="CONFIRMED">CONFIRMED</option>
          <option value="CHECKED_IN">CHECKED_IN</option>
          <option value="CHECKED_OUT">CHECKED_OUT</option>
          <option value="CANCELLED">CANCELLED</option>
          <option value="EXPIRED">EXPIRED</option>
        </select>
        <button type="button" onClick={() => byStatusMutation.mutate(statusFilter)} className={secondaryButtonClass}>
          Get By Status
        </button>
        <input value={lookupReservationId} onChange={(event) => setLookupReservationId(event.target.value)} type="number" placeholder="Enter reservation ID (e.g. 42)" className="rounded-md border border-slate-300 px-3 py-2 text-sm" />
        <button
          type="button"
          onClick={() => {
            if (!lookupReservationId) {
              toast.error("Reservation ID is required.");
              return;
            }
            byIdMutation.mutate(Number(lookupReservationId));
          }}
          className={primaryButtonClass}
        >
          Get By ID
        </button>
      </div>

      {byIdMutation.data ? (
        <div className="rounded-lg border border-slate-200 bg-white p-4 text-sm shadow-sm">
          <p>ID: {byIdMutation.data.reservationId}</p>
          <p>User: {byIdMutation.data.userName}</p>
          <p>Room Type: {byIdMutation.data.roomTypeName}</p>
          <p>Status: {byIdMutation.data.reservationStatus}</p>
          <p>Guest Count: {byIdMutation.data.guestCount}</p>
          <p>Guests: {formatGuestNames(byIdMutation.data.guests)}</p>
        </div>
      ) : null}

      {byStatusMutation.data ? (
        <DataTable
          title="Reservations By Status"
          data={byStatusMutation.data}
          rowKey={(row) => `status-${row.reservationId}`}
          searchTextExtractor={(row) => `${row.reservationId} ${row.userName} ${formatGuestNames(row.guests)}`}
          emptyMessage="No reservations found for selected status."
          columns={[
            { key: "id", header: "ID", render: (row) => row.reservationId },
            { key: "customer", header: "Customer", render: (row) => row.userName },
            { key: "guestCount", header: "Guest Count", render: (row) => row.guestCount },
            { key: "guests", header: "Guests", render: (row) => formatGuestNames(row.guests) },
            { key: "status", header: "Status", render: (row) => <StatusBadge value={row.reservationStatus} /> },
          ]}
        />
      ) : null}

      <DataTable
        title="Reservation Management"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load reservations." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No reservations found."
        rowKey={(row) => row.reservationId}
        searchPlaceholder="Search by user, room type, status"
        searchTextExtractor={(row) => `${row.userName} ${row.roomTypeName} ${row.reservationStatus} ${formatGuestNames(row.guests)}`}
        columns={[
          { key: "id", header: "ID", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "customer", header: "Customer", render: (row) => row.userName, sortValue: (row) => row.userName },
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
          {
            key: "actions",
            header: "Actions",
            render: (row) => (
              <div className="flex flex-wrap gap-2">
                {["PENDING", "CONFIRMED"].includes(row.reservationStatus) ? (
                  <button
                    type="button"
                    className={tableDangerButtonClass}
                    onClick={() => setPendingCancelReservationId(row.reservationId)}
                    disabled={cancelMutation.isPending}
                  >
                    Cancel
                  </button>
                ) : null}
              </div>
            ),
          },
        ]}
      />

      <ConfirmDialog
        open={Boolean(pendingCancelReservationId)}
        title="Cancel Reservation"
        description="This will cancel the reservation and release the room. Continue?"
        confirmLabel="Cancel Reservation"
        isConfirming={cancelMutation.isPending}
        onCancel={() => setPendingCancelReservationId(null)}
        onConfirm={() => {
          if (pendingCancelReservationId) {
            cancelMutation.mutate(pendingCancelReservationId, {
              onSettled: () => setPendingCancelReservationId(null),
            });
          }
        }}
      />
    </div>
  );
}
