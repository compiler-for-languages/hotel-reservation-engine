import { cn } from "@/utils/cn";

const palette: Record<string, string> = {
  ACTIVE: "bg-emerald-50 text-emerald-700 border-emerald-200",
  SUCCESS: "bg-emerald-50 text-emerald-700 border-emerald-200",
  AVAILABLE: "bg-emerald-50 text-emerald-700 border-emerald-200",
  CONFIRMED: "bg-emerald-50 text-emerald-700 border-emerald-200",
  CHECKED_IN: "bg-sky-50 text-sky-700 border-sky-200",
  CHECKED_OUT: "bg-slate-50 text-slate-700 border-slate-200",
  PROCESSING: "bg-amber-50 text-amber-700 border-amber-200",
  PENDING: "bg-amber-50 text-amber-700 border-amber-200",
  ASSIGNED: "bg-indigo-50 text-indigo-700 border-indigo-200",
  INACTIVE: "bg-slate-50 text-slate-700 border-slate-200",
  CANCELLED: "bg-rose-50 text-rose-700 border-rose-200",
  FAILED: "bg-rose-50 text-rose-700 border-rose-200",
  EXPIRED: "bg-rose-50 text-rose-700 border-rose-200",
  REFUNDED: "bg-violet-50 text-violet-700 border-violet-200",
  OCCUPIED: "bg-sky-50 text-sky-700 border-sky-200",
  MAINTENANCE: "bg-orange-50 text-orange-700 border-orange-200",
  OUT_OF_SERVICE: "bg-rose-50 text-rose-700 border-rose-200",
};

export const StatusBadge = ({ value }: { value: string }) => (
  <span className={cn("inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold", palette[value] ?? "bg-slate-50 text-slate-700 border-slate-200")}>{value}</span>
);