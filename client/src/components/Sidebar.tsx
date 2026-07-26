import { NavLink } from "react-router-dom";
import {
  Zap,
  HelpCircle,
  LogOut,
  LayoutGrid,
  FileText,
  List,
  Users,
  Eye,
  Settings,
  IdCard,
  KeyRound,
  CreditCard,
} from "lucide-react";
import useAuth from "../app/hooks/useAuth";

const adminNavItems = [
  { label: "Dashboard", href: "/dashboard", icon: LayoutGrid },
  { label: "Invoices", href: "/invoices", icon: FileText },
  { label: "Subscriptions", href: "/subscriptions", icon: List },
  { label: "Clients", href: "/clients", icon: Users, badge: 7 },
  { label: "Revenue", href: "/revenue", icon: Eye },
  { label: "Settings", href: "/settings", icon: Settings },
  { label: "Team", href: "/team", icon: IdCard },
  { label: "API Keys", href: "/api-keys", icon: KeyRound },
];

const customerNavItems = [
  { label: "Dashboard", href: "/dashboard", icon: LayoutGrid },
  
  { label: "My Subscription", href: "/dashboard/subscription", icon: List },
  { label: "Payment & Invoices", href: "/dashboard/payment", icon: CreditCard },
  { label: "My Invoices", href: "/dashboard/invoices", icon: FileText },
  { label: "Settings", href: "/dashboard/settings", icon: Settings },
];

export default function Sidebar() {
  const { user, logout} = useAuth();
  const isAdmin = user?.role === "ADMIN";
  const navItems = isAdmin ? adminNavItems : customerNavItems;
  console.log("FROM API CONTEXT: ", user);

  const initials = user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : "";

  return (
    <aside className="sticky top-0 flex h-screen w-[var(--sidebar-width)] flex-col justify-between border-r border-[var(--border)] bg-[var(--bg-sidebar)] px-4 py-6">
      <div>
        {/* Logo */}
        <div className="mb-7 flex items-center gap-2 px-2">
          <Zap className="h-5 w-5 fill-[var(--warning-text)] text-[var(--warning-text)]" />
          <span className="text-lg font-semibold text-white">
            Bill<span className="text-[var(--primary)]">Flow</span>
          </span>
        </div>

        {/* User profile */}
        <div className="mb-6 flex items-center gap-3 px-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-[var(--bg-card)] text-xs font-semibold text-[var(--text-primary)]">
            {initials}
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-[var(--text-primary)]">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="truncate text-xs text-[var(--text-secondary)]">
              {isAdmin ? "Billing Admin" : "Customer"}
            </p>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex flex-col gap-1">
          {navItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.href}
                to={item.href}
                end={item.href === "/dashboard"}
                className={({ isActive }) =>
                  `flex items-center justify-between rounded-[var(--radius-button)] px-3 py-2.5 text-sm transition-colors ${
                    isActive
                      ? "border-l-2 border-[var(--primary)] bg-[var(--primary-glow)] font-medium text-[var(--text-primary)]"
                      : "border-l-2 border-transparent text-[var(--text-secondary)] hover:bg-[var(--bg-card)] hover:text-[var(--text-primary)]"
                  }`
                }
              >
                <span className="flex items-center gap-3">
                  <Icon className="h-4 w-4" />
                  {item.label}
                </span>

                {/* {item.icon !== undefined && (
                  <span className="flex h-5 min-w-5 items-center justify-center rounded-[var(--radius-badge)] bg-[var(--error-text)] px-1.5 text-[11px] font-semibold text-white">
                    {item.icon}
                  </span>
                )} */}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Footer actions */}
      <div className="flex flex-col gap-4">
        {!isAdmin && (
          <button
            type="button"
            className="w-full rounded-[var(--radius-button)] bg-[var(--primary)] py-2.5 text-sm font-semibold text-white hover:bg-[var(--primary-hover)] cursor-pointer"
          >
            Upgrade Plan
          </button>
        )}

        <div className="flex flex-col gap-1">
          <button
            type="button"
            className="flex items-center gap-3 rounded-[var(--radius-button)] px-3 py-2 text-sm text-[var(--text-secondary)] hover:bg-[var(--bg-card)] hover:text-[var(--text-primary)] cursor-pointer"
          >
            <HelpCircle className="h-4 w-4" />
            Help Center
          </button>
          <button
            type="button"
            onClick={logout}
            className="flex items-center gap-3 rounded-[var(--radius-button)] px-3 py-2 text-sm text-[var(--text-secondary)] hover:bg-[var(--bg-card)] hover:text-[var(--text-primary)] cursor-pointer"
          >
            <LogOut className="h-4 w-4" />
            Logout
          </button>
        </div>
      </div>
    </aside>
  );
}