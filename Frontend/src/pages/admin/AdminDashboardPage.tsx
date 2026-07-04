import { useQuery } from "@tanstack/react-query";
import { DataTable } from "@/components/common/DataTable";
import { ErrorState } from "@/components/common/ErrorState";
import { OccupancyCalendar } from "@/components/common/OccupancyCalendar";
import { PageHeader } from "@/components/common/PageHeader";
import { StatsSkeleton } from "@/components/common/StatsSkeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PaymentService } from "@/services/PaymentService";
import { ReservationService } from "@/services/ReservationService";
import { RoomService } from "@/services/RoomService";
import { UserService } from "@/services/UserService";
import { formatCurrency, formatDate, formatGuestNames } from "@/utils/format";

export default function AdminDashboardPage() {
  const {
    data: users = [],
    isLoading: usersLoading,
    error: usersError,
    refetch: refetchUsers,
  } = useQuery({
    queryKey: ["dashboardUsers"],
    queryFn: UserService.getAllUsers,
  });

  const {
    data: rooms = [],
    isLoading: roomsLoading,
    error: roomsError,
    refetch: refetchRooms,
  } = useQuery({
    queryKey: ["dashboardRooms"],
    queryFn: RoomService.getAllRooms,
  });

  const {
    data: reservations = [],
    isLoading: reservationsLoading,
    error: reservationsError,
    refetch: refetchReservations,
  } = useQuery({
    queryKey: ["dashboardReservations"],
    queryFn: ReservationService.getAllReservations,
  });

  const {
    data: payments = [],
    isLoading: paymentsLoading,
    error: paymentsError,
    refetch: refetchPayments,
  } = useQuery({
    queryKey: ["dashboardPayments"],
    queryFn: PaymentService.getAllPayments,
  });

  const statsLoading = usersLoading || roomsLoading || reservationsLoading || paymentsLoading;
  const statsError = usersError || roomsError || reservationsError || paymentsError;

  const totalRevenue = payments
    .filter((item) => item.paymentStatus === "SUCCESS")
    .reduce((sum, item) => sum + (typeof item.amount === 'number' ? item.amount : parseFloat(item.amount)), 0);

  const totalRefundAmount = payments
    .filter((item) => item.paymentStatus === "REFUNDED")
    .reduce((sum, item) => sum + (typeof item.amount === 'number' ? item.amount : parseFloat(item.amount)), 0);

  const totalReservations = reservations.length;
  const pendingReservations = reservations.filter((item) => item.reservationStatus === "PENDING").length;
  const cancelledReservations = reservations.filter((item) => item.reservationStatus === "CANCELLED").length;

  const activeRooms = rooms.filter(
    (item) => item.roomStatus === "AVAILABLE" || item.roomStatus === "OCCUPIED"
  ).length;
  const availableRooms = rooms.filter((item) => item.roomStatus === "AVAILABLE").length;
  const occupiedRooms = rooms.filter((item) => item.roomStatus === "OCCUPIED").length;

  const statCards = [
    { label: "Total Users", value: users.length },
    { label: "Customers", value: users.filter((item) => item.role === "CUSTOMER").length },
    { label: "Receptionists", value: users.filter((item) => item.role === "RECEPTIONIST").length },
    { label: "Total Revenue Collected", value: formatCurrency(totalRevenue) },
    { label: "Total Refund Amount", value: formatCurrency(totalRefundAmount) },
    { label: "Total Reservations", value: totalReservations },
    { label: "Pending Reservations", value: pendingReservations },
    { label: "Cancelled Reservations", value: cancelledReservations },
    { label: "Active Rooms", value: activeRooms },
    { label: "Available Rooms", value: availableRooms },
    { label: "Occupied Rooms", value: occupiedRooms },
  ];

  return (
    <div className="space-y-6">
      <PageHeader title="Admin Dashboard" description="Enterprise snapshot of users, inventory, reservations, and payments." />

      {statsError ? (
        <ErrorState
          message="Unable to load one or more dashboard datasets."
          onRetry={() => {
            void refetchUsers();
            void refetchRooms();
            void refetchReservations();
            void refetchPayments();
          }}
        />
      ) : null}

      {statsLoading ? <StatsSkeleton count={11} /> : null}

      {!statsLoading ? (
        <div className="grid gap-4 md:grid-cols-3 xl:grid-cols-4">
          {statCards.map((card) => (
            <div key={card.label} className="card-elegant p-5">
              <p className="text-sm font-medium text-slate-500">{card.label}</p>
              <p className="mt-2 text-2xl font-bold text-slate-900">{card.value}</p>
            </div>
          ))}
        </div>
      ) : null}

      <OccupancyCalendar reservations={reservations} />

      <DataTable
        title="Recent Reservations"
        data={[...reservations].sort((a, b) => (a.bookingTime < b.bookingTime ? 1 : -1)).slice(0, 8)}
        isLoading={reservationsLoading}
        errorMessage={reservationsError ? "Unable to load recent reservations." : null}
        onRetry={() => {
          void refetchReservations();
        }}
        emptyMessage="No recent reservations available."
        rowKey={(row) => row.reservationId}
        searchTextExtractor={(row) => `${row.userName} ${row.roomTypeName} ${row.reservationStatus}`}
        columns={[
          { key: "id", header: "ID", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "customer", header: "Customer", render: (row) => row.userName, sortValue: (row) => row.userName },
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
        data={[...payments].sort((a, b) => ((a.paidAt || "") < (b.paidAt || "") ? 1 : -1)).slice(0, 8)}
        isLoading={paymentsLoading}
        errorMessage={paymentsError ? "Unable to load recent payments." : null}
        onRetry={() => {
          void refetchPayments();
        }}
        emptyMessage="No recent payments available."
        rowKey={(row) => row.paymentId}
        searchTextExtractor={(row) => `${row.paymentId} ${row.paymentStatus} ${row.reservationId}`}
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
