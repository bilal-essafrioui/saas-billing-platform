import { Outlet } from "react-router-dom";
import Sidebar from "../components/Sidebar";
export default function DashboardLayout() {
  return (
    <div className="flex min-h-screen bg-[var(--bg-page)]">
      <Sidebar />
      <main className="flex-1 overflow-y-auto px-10 py-10">
        <Outlet />
      </main>
    </div>
  );
}