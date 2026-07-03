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
    data: dashboardData,
    isLoading: dashboardLoading,
    error: dashboardError,
    refetch: refetchDashboard,
  } = useQuery({
    queryKey: ["customerDashboard", user?.userId],
    enabled: Boolean(user?.userId),
    queryFn: async () => {
      const reservations = await ReservationService.getReservationsByUser(user!.userId);
      const paymentResults = await Promise.allSettled(
        reservations.map((reservation) => PaymentService.getPaymentByReservation(reservation.reservationId))
      );
      const payments = paymentResults
        .filter((entry): entry is PromiseFulfilledResult<PaymentResponseDTO> => entry.status === "fulfilled")
        .map((entry) => entry.value);

      return { reservations, payments };
    },
    refetchInterval: 30000, // Refetch every 30 seconds to catch status changes (PENDING -> EXPIRED)
  });

  const reservations = dashboardData?.reservations ?? [];
  const payments = dashboardData?.payments ?? [];

  const confirmedBookings = reservations.filter((item) => item.reservationStatus === "CONFIRMED").length;

  return (
    <div className="space-y-6">
      <PageHeader title="Customer Dashboard" description="Track reservations, payments, and booking activity." />

      {dashboardLoading ? <StatsSkeleton count={3} /> : null}

      {dashboardError ? (
        <ErrorState
          message="Unable to load one or more dashboard sections."
          onRetry={() => {
            void refetchDashboard();
          }}
        />
      ) : null}

      {!dashboardLoading ? <div className="grid gap-4 md:grid-cols-3">
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Total Reservations</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{reservations.length}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
        </div>
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Confirmed Bookings</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{confirmedBookings}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </div>
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Total Payments</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{payments.length}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
              </svg>
            </div>
          </div>
        </div>
      </div> : null}

      <DataTable
        title="Recent Reservations"
        data={[...reservations].sort((a, b) => (a.bookingTime < b.bookingTime ? 1 : -1)).slice(0, 6)}
        isLoading={dashboardLoading}
        errorMessage={dashboardError ? "Unable to load recent reservations." : null}
        onRetry={() => {
          void refetchDashboard();
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
        isLoading={dashboardLoading}
        errorMessage={dashboardError ? "Unable to load recent payments." : null}
        onRetry={() => {
          void refetchDashboard();
        }}
        emptyMessage="No payments found yet."
        rowKey={(row) => row.paymentId}
        searchTextExtractor={(row) => `${row.paymentId} ${row.paymentStatus}`}
        columns={[
          { key: "id", header: "Payment", render: (row) => row.paymentId, sortValue: (row) => row.paymentId },
          { key: "reservation", header: "Reservation", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "amount", header: "Amount", render: (row) => formatCurrency(row.amount, row.currency), sortValue: (row) => row.amount },
          { key: "method", header: "Method", render: (row) => row.paymentMethod, sortValue: (row) => row.paymentMethod },
          { key: "gatewayPaymentId", header: "Gateway Payment ID", render: (row) => row.gatewayPaymentId || "-", sortValue: (row) => row.gatewayPaymentId || "" },
          { key: "gatewaySignature", header: "Gateway Signature", render: (row) => row.gatewaySignature || "-", sortValue: (row) => row.gatewaySignature || "" },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.paymentStatus} />, sortValue: (row) => row.paymentStatus },
        ]}
      />
    </div>
  );
}