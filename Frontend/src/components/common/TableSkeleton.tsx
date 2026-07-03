export const TableSkeleton = ({ rows = 6 }: { rows?: number }) => (
  <div className="space-y-2">
    {Array.from({ length: rows }).map((_, index) => (
      <div key={index} className="h-10 animate-pulse rounded-md bg-slate-100" />
    ))}
  </div>
);