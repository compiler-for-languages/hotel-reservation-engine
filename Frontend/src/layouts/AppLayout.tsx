import { Outlet } from "react-router-dom";
import { Sidebar } from "@/components/layout/Sidebar";
import { TopNavbar } from "@/components/layout/TopNavbar";
import { useUiStore } from "@/store/uiStore";
import { cn } from "@/utils/cn";

export const AppLayout = () => {
  const sidebarCollapsed = useUiStore((state) => state.sidebarCollapsed);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50/30 text-slate-900">
      <TopNavbar />
      <Sidebar />
      <main
        className={cn(
          "pt-20 pr-4 pb-6 pl-4 transition-all duration-300 md:pr-6 md:pb-8 md:pl-6",
          sidebarCollapsed ? "ml-20" : "ml-64"
        )}
      >
        <Outlet />
      </main>
    </div>
  );
};