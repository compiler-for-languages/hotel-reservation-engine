import { useNavigate } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";
import { roleLabel } from "@/utils/role";
import { clearApplicationSession } from "@/utils/session";

export const TopNavbar = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);

  const handleLogout = () => {
    clearApplicationSession();
    navigate("/login", { replace: true });
  };

  return (
    <header className="fixed top-0 right-0 left-0 z-30 glass-effect border-b border-slate-200/50">
      <div className="flex h-16 items-center justify-between px-4 md:px-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl gradient-primary text-white font-bold text-lg shadow-lg">
            H
          </div>
          <div>
            <p className="text-xs text-slate-500 font-medium">Welcome</p>
            <h1 className="text-sm font-semibold text-slate-900 md:text-base tracking-tight">Hotel Reservation Engine</h1>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-semibold text-slate-900">{user ? `${user.firstName} ${user.lastName}` : "-"}</p>
            <p className="text-xs text-slate-500">{user?.email ?? ""}</p>
          </div>

          <span className="hidden sm:inline-flex rounded-full bg-gradient-to-r from-indigo-50 to-purple-50 border border-indigo-200 px-3 py-1 text-xs font-semibold text-indigo-700">
            {user ? roleLabel[user.role] : "-"}
          </span>

          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-white font-semibold shadow-md">
            {user ? `${user.firstName[0] ?? ""}${user.lastName[0] ?? ""}` : "NA"}
          </div>

          <button
            type="button"
            onClick={handleLogout}
            aria-label="Logout"
            className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-all hover:bg-slate-50 hover:border-slate-300 hover:shadow-sm"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};