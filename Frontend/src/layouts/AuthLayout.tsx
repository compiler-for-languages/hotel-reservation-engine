import { Outlet } from "react-router-dom";

export const AuthLayout = () => (
  <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
    <div className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-sm md:p-8">
      <Outlet />
    </div>
  </div>
);