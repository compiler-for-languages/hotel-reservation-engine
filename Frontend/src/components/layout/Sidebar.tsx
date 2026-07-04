import { NavLink } from "react-router-dom";
import { menuByRole } from "@/routes/menuConfig";
import { useAuthStore } from "@/store/authStore";
import { useUiStore } from "@/store/uiStore";
import { cn } from "@/utils/cn";

export const Sidebar = () => {
  const user = useAuthStore((state) => state.user);
  const sidebarCollapsed = useUiStore((state) => state.sidebarCollapsed);
  const toggleSidebar = useUiStore((state) => state.toggleSidebar);

  const menu = user ? menuByRole[user.role] : [];

  return (
    <aside
      className={cn(
        "fixed top-16 left-0 z-20 h-[calc(100vh-4rem)] border-r border-slate-200 bg-white transition-all duration-300",
        sidebarCollapsed ? "w-20" : "w-64"
      )}
    >
      <div className="flex h-full flex-col">
        <div className="border-b border-slate-200 p-3">
          <button
            type="button"
            onClick={toggleSidebar}
            aria-expanded={!sidebarCollapsed}
            aria-label={sidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-left text-sm font-medium text-black transition-all hover:bg-slate-100"
          >
            {sidebarCollapsed ? "→" : "← Collapse"}
          </button>
        </div>

        <nav
          className="flex-1 space-y-1 overflow-y-auto p-3"
          aria-label="Role navigation menu"
        >
          {menu.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                cn(
                  "flex items-center rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200",
                  isActive
                    ? "bg-black text-white shadow-md"
                    : "text-slate-700 hover:bg-slate-100 hover:text-black",
                  sidebarCollapsed && "justify-center"
                )
              }
              title={item.label}
            >
              {sidebarCollapsed ? item.label.slice(0, 2) : item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </aside>
  );
};