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
    <header className="fixed top-0 right-0 left-0 z-30 border-b border-slate-200 bg-white">
      <div className="flex h-16 items-center justify-between px-4 md:px-6">
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-md bg-slate-900 text-sm font-semibold text-white">H</div>
          <div>
            <p className="text-xs text-slate-500">Application</p>
            <h1 className="text-sm font-semibold text-slate-900 md:text-base">Hotel Reservation Engine</h1>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-medium text-slate-900">{user ? `${user.firstName} ${user.lastName}` : "-"}</p>
            <p className="text-xs text-slate-500">{user?.email ?? ""}</p>
          </div>

          <span className="rounded-full border border-slate-300 px-3 py-1 text-xs font-medium text-slate-700">
            {user ? roleLabel[user.role] : "-"}
          </span>

          <div className="flex h-9 w-9 items-center justify-center rounded-full border border-slate-300 bg-slate-50 text-xs font-semibold text-slate-700">
            {user ? `${user.firstName[0] ?? ""}${user.lastName[0] ?? ""}` : "NA"}
          </div>

          <button
            type="button"
            onClick={handleLogout}
            aria-label="Logout"
            className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};