import { useQuery } from "@tanstack/react-query";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PaymentService } from "@/services/PaymentService";
import { ReservationService } from "@/services/ReservationService";
import { useAuthStore } from "@/store/authStore";
import { formatCurrency, formatDate } from "@/utils/format";

interface BookingHistoryRow {
  reservationId: number;
  roomTypeName: string;
  checkInDate: string;
  checkOutDate: string;
  reservationStatus: string;
  paymentStatus: string;
  amount: number;
  currency: string;
}

export default function BookingHistoryPage() {
  const user = useAuthStore((state) => state.user);

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["bookingHistory", user?.userId],
    enabled: Boolean(user?.userId),
    queryFn: async (): Promise<BookingHistoryRow[]> => {
      const reservations = await ReservationService.getReservationsByUser(user!.userId);

      const payments = await Promise.allSettled(
        reservations.map((reservation) => PaymentService.getPaymentByReservation(reservation.reservationId))
      );

      return reservations.map((reservation) => {
        const payment = payments.find(
          (entry) => entry.status === "fulfilled" && entry.value.reservationId === reservation.reservationId
        );

        if (payment?.status === "fulfilled") {
          return {
            reservationId: reservation.reservationId,
            roomTypeName: reservation.roomTypeName,
            checkInDate: reservation.checkInDate,
            checkOutDate: reservation.checkOutDate,
            reservationStatus: reservation.reservationStatus,
            paymentStatus: payment.value.paymentStatus,
            amount: payment.value.amount,
            currency: payment.value.currency,
          };
        }

        return {
          reservationId: reservation.reservationId,
          roomTypeName: reservation.roomTypeName,
          checkInDate: reservation.checkInDate,
          checkOutDate: reservation.checkOutDate,
          reservationStatus: reservation.reservationStatus,
          paymentStatus: "PENDING",
          amount: 0,
          currency: "INR",
        };
      });
    },
  });

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-semibold text-slate-900">Booking History</h2>

      <DataTable
        title="Booking and Payment Timeline"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load booking history." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No booking history available yet."
        rowKey={(row) => row.reservationId}
        searchPlaceholder="Search by reservation ID or room type"
        searchTextExtractor={(row) => `${row.reservationId} ${row.roomTypeName} ${row.reservationStatus} ${row.paymentStatus}`}
        columns={[
          {
            key: "reservationId",
            header: "Reservation ID",
            render: (row) => row.reservationId,
            sortValue: (row) => row.reservationId,
          },
          { key: "roomType", header: "Room Type", render: (row) => row.roomTypeName, sortValue: (row) => row.roomTypeName },
          { key: "checkIn", header: "Check In", render: (row) => formatDate(row.checkInDate), sortValue: (row) => row.checkInDate },
          { key: "checkOut", header: "Check Out", render: (row) => formatDate(row.checkOutDate), sortValue: (row) => row.checkOutDate },
          {
            key: "reservationStatus",
            header: "Reservation",
            render: (row) => <StatusBadge value={row.reservationStatus} />,
            sortValue: (row) => row.reservationStatus,
          },
          {
            key: "paymentStatus",
            header: "Payment",
            render: (row) => <StatusBadge value={row.paymentStatus} />,
            sortValue: (row) => row.paymentStatus,
          },
          {
            key: "amount",
            header: "Amount",
            render: (row) => formatCurrency(row.amount, row.currency),
            sortValue: (row) => row.amount,
          },
        ]}
      />
    </div>
  );
}