import { cn } from "@/utils/cn";

const palette: Record<string, string> = {
  ACTIVE: "bg-emerald-100 text-emerald-700",
  SUCCESS: "bg-emerald-100 text-emerald-700",
  AVAILABLE: "bg-emerald-100 text-emerald-700",
  CONFIRMED: "bg-emerald-100 text-emerald-700",
  CHECKED_IN: "bg-sky-100 text-sky-700",
  CHECKED_OUT: "bg-slate-200 text-slate-700",
  PROCESSING: "bg-amber-100 text-amber-700",
  PENDING: "bg-amber-100 text-amber-700",
  ASSIGNED: "bg-indigo-100 text-indigo-700",
  INACTIVE: "bg-slate-200 text-slate-700",
  CANCELLED: "bg-rose-100 text-rose-700",
  FAILED: "bg-rose-100 text-rose-700",
  EXPIRED: "bg-rose-100 text-rose-700",
  REFUNDED: "bg-violet-100 text-violet-700",
  OCCUPIED: "bg-sky-100 text-sky-700",
  MAINTENANCE: "bg-orange-100 text-orange-700",
  OUT_OF_SERVICE: "bg-rose-100 text-rose-700",
};

export const StatusBadge = ({ value }: { value: string }) => (
  <span className={cn("rounded-full px-2 py-1 text-xs font-medium", palette[value] ?? "bg-slate-100 text-slate-700")}>{value}</span>
);