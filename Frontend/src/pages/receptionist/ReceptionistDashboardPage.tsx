import { useQuery } from "@tanstack/react-query";
import { ErrorState } from "@/components/common/ErrorState";
import { PageHeader } from "@/components/common/PageHeader";
import { StatsSkeleton } from "@/components/common/StatsSkeleton";
import { ReceptionService } from "@/services/ReceptionService";

export default function ReceptionistDashboardPage() {
  const {
    data,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["receptionDashboard"],
    queryFn: ReceptionService.getDashboard,
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Reception Dashboard" description="Operational snapshot for front desk activity." />

      {error ? (
        <ErrorState
          message="Unable to load reception dashboard."
          onRetry={() => {
            void refetch();
          }}
        />
      ) : null}

      {isLoading ? <StatsSkeleton count={5} /> : null}

      {!isLoading ? <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Today's Arrivals</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{data?.todayArrivals ?? 0}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Today's Departures</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{data?.todayDepartures ?? 0}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Current Guests</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{data?.currentGuests ?? 0}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Available Rooms</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{data?.availableRooms ?? 0}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Occupied Rooms</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{data?.occupiedRooms ?? 0}</p>
        </div>
      </div> : null}
    </div>
  );
}