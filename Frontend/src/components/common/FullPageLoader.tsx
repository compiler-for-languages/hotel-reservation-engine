import { Spinner } from "@/components/common/Spinner";

export const FullPageLoader = ({ label = "Loading application..." }: { label?: string }) => (
  <div className="flex min-h-screen items-center justify-center bg-slate-50">
    <div className="flex items-center gap-3 rounded-md border border-slate-200 bg-white px-5 py-3 text-sm text-slate-700">
      <Spinner />
      <span>{label}</span>
    </div>
  </div>
);