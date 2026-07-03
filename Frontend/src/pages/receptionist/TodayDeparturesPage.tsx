import { useQuery } from "@tanstack/react-query";
import { DataTable } from "@/components/common/DataTable";
import { PageHeader } from "@/components/common/PageHeader";
import { ReceptionService } from "@/services/ReceptionService";
import { formatDate, formatDateTime } from "@/utils/format";

export default function TodayDeparturesPage() {
  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["todayDepartures"],
    queryFn: ReceptionService.getTodayDepartures,
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Today's Departures" description="Guests scheduled to check out today." />

      <DataTable
        title="Departures"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load today's departures." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No departures scheduled for today."
        rowKey={(row) => row.reservationId}
        searchPlaceholder="Search by customer, room number, room type"
        searchTextExtractor={(row) => `${row.customerName} ${row.roomNumber} ${row.roomType} ${row.guestNames ?? ""}`}
        columns={[
          { key: "reservationId", header: "Reservation ID", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          { key: "customer", header: "Customer", render: (row) => row.customerName, sortValue: (row) => row.customerName },
          { key: "roomNumber", header: "Room", render: (row) => row.roomNumber, sortValue: (row) => row.roomNumber },
          { key: "roomType", header: "Room Type", render: (row) => row.roomType, sortValue: (row) => row.roomType },
          { key: "guestCount", header: "Guest Count", render: (row) => row.guestCount, sortValue: (row) => row.guestCount },
          {
            key: "guests",
            header: "Guests",
            render: (row) => (row.guestNames?.trim() ? row.guestNames : "-"),
            sortValue: (row) => row.guestNames ?? "",
          },
          { key: "checkOutDate", header: "Scheduled Out", render: (row) => formatDate(row.checkOutDate), sortValue: (row) => row.checkOutDate },
          {
            key: "actualCheckIn",
            header: "Checked In At",
            render: (row) => (row.actualCheckIn ? formatDateTime(row.actualCheckIn) : "-"),
            sortValue: (row) => row.actualCheckIn || "",
          },
        ]}
      />
    </div>
  );
}
