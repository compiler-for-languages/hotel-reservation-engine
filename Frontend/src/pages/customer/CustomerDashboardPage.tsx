import { useQuery } from "@tanstack/react-query";
import { DataTable } from "@/components/common/DataTable";
import { ErrorState } from "@/components/common/ErrorState";
import { PageHeader } from "@/components/common/PageHeader";
import { StatsSkeleton } from "@/components/common/StatsSkeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PaymentService } from "@/services/PaymentService";
import { ReservationService } from "@/services/ReservationService";
import { useAuthStore } from "@/store/authStore";
import type { PaymentResponseDTO } from "@/types/api";
import { formatCurrency, formatDate, formatGuestNames } from "@/utils/format";

export default function CustomerDashboardPage() {
  const user = useAuthStore((state) => state.user);

  const {
    data: reservations = [],
    isLoading: reservationsLoading,
    error: reservationsError,
    refetch: refetchReservations,
  } = useQuery({
    queryKey: ["customerDashboardReservations", user?.userId],
    enabled: Boolean(user?.userId),
    queryFn: () => ReservationService.getReservationsByUser(user!.userId),
  });

  const {
    data: payments = [],
    isLoading: paymentsLoading,
    error: paymentsError,
    refetch: refetchPayments,
  } = useQuery({
    queryKey: ["customerDashboardPayments", user?.userId],
    enabled: Boolean(user?.userId),
    queryFn: async () => {
      const paymentResults = await Promise.allSettled(
        reservations.map((reservation) => PaymentService.getPaymentByReservation(reservation.reservationId))
      );
      return paymentResults
        .filter((entry): entry is PromiseFulfilledResult<PaymentResponseDTO> => entry.status === "fulfilled")
        .map((entry) => entry.value);
    },
  });

  const confirmedBookings = reservations.filter((item) => item.reservationStatus === "CONFIRMED").length;

  return (
    <div className="space-y-6">
      <PageHeader title="Customer Dashboard" description="Track reservations, payments, and booking activity." />

      {reservationsLoading ? <StatsSkeleton count={3} /> : null}

      {reservationsError || paymentsError ? (
        <ErrorState
          message="Unable to load one or more dashboard sections."
          onRetry={() => {
            void refetchReservations();
            void refetchPayments();
          }}
        />
      ) : null}

      {!reservationsLoading ? <div className="grid gap-4 md:grid-cols-3">
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Total Reservations</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{reservations.length}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Confirmed Bookings</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{confirmedBookings}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Total Payments</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{payments.length}</p>
        </div>
      </div> : null}

      <DataTable
        title="Recent Reservations"
        data={[...reservations].sort((a, b) => (a.bookingTime < b.bookingTime ? 1 : -1)).slice(0, 6)}
        isLoading={reservationsLoading}
        errorMessage={reservationsError ? "Unable to load recent reservations." : null}
        onRetry={() => {
          void refetchReservations();
        }}
        emptyMessage="No reservations found yet."
        rowKey={(row) => row.reservationId}
        searchTextExtractor={(row) => `${row.reservationId} ${row.roomTypeName} ${row.reservationStatus}`}
        columns={[
          { key: "id", header: "Reservation", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "roomType", header: "Room Type", render: (row) => row.roomTypeName, sortValue: (row) => row.roomTypeName },
          { key: "guestCount", header: "Guest Count", render: (row) => row.guestCount, sortValue: (row) => row.guestCount },
          { key: "guests", header: "Guests", render: (row) => formatGuestNames(row.guests), sortValue: (row) => formatGuestNames(row.guests) },
          { key: "checkIn", header: "Check In", render: (row) => formatDate(row.checkInDate), sortValue: (row) => row.checkInDate },
          { key: "checkOut", header: "Check Out", render: (row) => formatDate(row.checkOutDate), sortValue: (row) => row.checkOutDate },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.reservationStatus} />, sortValue: (row) => row.reservationStatus },
        ]}
      />

      <DataTable
        title="Recent Payments"
        data={[...payments].sort((a, b) => ((a.paidAt || "") < (b.paidAt || "") ? 1 : -1)).slice(0, 6)}
        isLoading={paymentsLoading}
        errorMessage={paymentsError ? "Unable to load recent payments." : null}
        onRetry={() => {
          void refetchPayments();
        }}
        emptyMessage="No payments found yet."
        rowKey={(row) => row.paymentId}
        searchTextExtractor={(row) => `${row.paymentId} ${row.paymentStatus}`}
        columns={[
          { key: "id", header: "Payment", render: (row) => row.paymentId, sortValue: (row) => row.paymentId },
          { key: "reservation", header: "Reservation", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "amount", header: "Amount", render: (row) => formatCurrency(row.amount, row.currency), sortValue: (row) => row.amount },
          { key: "method", header: "Method", render: (row) => row.paymentMethod, sortValue: (row) => row.paymentMethod },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.paymentStatus} />, sortValue: (row) => row.paymentStatus },
        ]}
      />
    </div>
  );
}