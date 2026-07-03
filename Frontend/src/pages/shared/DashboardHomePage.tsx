interface DashboardHomePageProps {
  title: string;
  description: string;
}

export const DashboardHomePage = ({ title, description }: DashboardHomePageProps) => (
  <div className="space-y-6">
    <div>
      <h2 className="text-2xl font-semibold text-slate-900">{title}</h2>
      <p className="mt-1 text-sm text-slate-600">{description}</p>
    </div>

    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      {["Today's Arrivals", "Today's Departures", "Available Rooms", "Current Guests"].map((label) => (
        <div key={label} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">{label}</p>
          <p className="mt-3 text-3xl font-semibold text-slate-900">--</p>
        </div>
      ))}
    </div>

    <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h3 className="text-base font-semibold text-slate-900">Recent Activity</h3>
      <p className="mt-2 text-sm text-slate-600">
        Data widgets will be connected in subsequent modules. Module 1 includes the secured dashboard shell and routing.
      </p>
    </div>
  </div>
);