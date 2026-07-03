export const StatsSkeleton = ({ count = 4 }: { count?: number }) => (
  <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
    {Array.from({ length: count }).map((_, index) => (
      <div key={index} className="h-24 animate-pulse rounded-lg border border-slate-200 bg-slate-100" />
    ))}
  </div>
);