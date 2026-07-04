import { useNavigate } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";
import { roleLabel } from "@/utils/role";
import { clearApplicationSession } from "@/utils/session";
import logo from "@/assets/logo.jpeg";

export const TopNavbar = () => {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);

  const handleLogout = () => {
    clearApplicationSession();
    navigate("/login", { replace: true });
  };

  return (
    <header className="fixed top-0 right-0 left-0 z-30 border-b border-slate-200 bg-white shadow-sm">
      <div className="flex h-16 items-center justify-between px-4 md:px-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-xl bg-black shadow-md">
            <img
              src={logo}
              alt="Logo"
              className="h-full w-full object-cover"
            />
          </div>

          <div>
            <p className="text-xs font-medium text-slate-500">
              Welcome
            </p>

            <h1 className="text-sm font-semibold tracking-tight text-black md:text-base">
              LOSM Reservation Engine
            </h1>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-semibold text-black">
              {user ? `${user.firstName} ${user.lastName}` : "-"}
            </p>

            <p className="text-xs text-slate-500">
              {user?.email ?? ""}
            </p>
          </div>

          <span className="hidden rounded-full border border-slate-300 bg-slate-100 px-3 py-1 text-xs font-semibold text-black sm:inline-flex">
            {user ? roleLabel[user.role] : "-"}
          </span>

          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-black font-semibold text-white shadow-md">
            {user
              ? `${user.firstName[0] ?? ""}${user.lastName[0] ?? ""}`
              : "NA"}
          </div>

          <button
            type="button"
            onClick={handleLogout}
            aria-label="Logout"
            className="rounded-lg border border-black bg-black px-4 py-2 text-sm font-medium text-white transition-all hover:bg-slate-800"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};