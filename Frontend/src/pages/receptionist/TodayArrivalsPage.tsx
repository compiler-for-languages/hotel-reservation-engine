import { useQuery } from "@tanstack/react-query";
import { DataTable } from "@/components/common/DataTable";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { ReceptionService } from "@/services/ReceptionService";
import { formatDate } from "@/utils/format";

export default function TodayArrivalsPage() {
  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["todayArrivals"],
    queryFn: ReceptionService.getTodayArrivals,
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Today's Arrivals" description="Confirmed reservations scheduled to check in today." />

      <DataTable
        title="Arrivals"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load today's arrivals." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No arrivals scheduled for today."
        rowKey={(row) => row.reservationId}
        searchPlaceholder="Search by customer, room type, phone"
        searchTextExtractor={(row) => `${row.customerName} ${row.roomType} ${row.phone} ${row.guestNames ?? ""} ${row.assignmentStatus}`}
        columns={[
          { key: "reservationId", header: "Reservation ID", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "customer", header: "Customer", render: (row) => row.customerName, sortValue: (row) => row.customerName },
          { key: "phone", header: "Mobile Number", render: (row) => row.phone, sortValue: (row) => row.phone },
          { key: "roomType", header: "Room Type", render: (row) => row.roomType, sortValue: (row) => row.roomType },
          { key: "guestCount", header: "Guest Count", render: (row) => row.guestCount, sortValue: (row) => row.guestCount },
          {
            key: "guests",
            header: "Guests",
            render: (row) => (row.guestNames?.trim() ? row.guestNames : "-"),
            sortValue: (row) => row.guestNames ?? "",
          },
          { key: "checkInDate", header: "Check In", render: (row) => formatDate(row.checkInDate), sortValue: (row) => row.checkInDate },
          { key: "checkOutDate", header: "Check Out", render: (row) => formatDate(row.checkOutDate), sortValue: (row) => row.checkOutDate },
          {
            key: "assignmentStatus",
            header: "Assignment",
            render: (row) => (row.roomAssigned ? <StatusBadge value={row.assignmentStatus} /> : <span className="text-xs">NOT_ASSIGNED</span>),
            sortValue: (row) => row.assignmentStatus || "",
          },
        ]}
      />
    </div>
  );
}
