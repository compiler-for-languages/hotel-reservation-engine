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

      {isLoading ? <StatsSkeleton count={4} /> : null}

      {!isLoading ? <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Today's Arrivals</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{data?.todayArrivals ?? 0}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-cyan-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
              </svg>
            </div>
          </div>
        </div>
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Today's Departures</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{data?.todayDepartures ?? 0}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-rose-500 to-pink-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
              </svg>
            </div>
          </div>
        </div>
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Available Rooms</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{data?.availableRooms ?? 0}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
          </div>
        </div>
        <div className="card-elegant p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">Occupied Rooms</p>
              <p className="mt-2 text-3xl font-bold text-slate-900">{data?.occupiedRooms ?? 0}</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 text-white">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
          </div>
        </div>
      </div> : null}
    </div>
  );
}