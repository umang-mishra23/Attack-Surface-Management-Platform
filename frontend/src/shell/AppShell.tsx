import {
  BellIcon,
  ChartBarIcon,
  Cog6ToothIcon,
  DocumentChartBarIcon,
  MagnifyingGlassIcon,
  RectangleStackIcon,
  ShieldCheckIcon,
  UserCircleIcon,
  WifiIcon,
  ExclamationTriangleIcon,
  ArrowRightOnRectangleIcon
} from "@heroicons/react/24/outline";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useState } from "react";
import { NewScanModal } from "../components/NewScanModal";
import { ScannerModeBadge } from "../components/ScannerModeBadge";

const items = [
  { to: "/", label: "Dashboard", icon: ChartBarIcon },
  { to: "/scans", label: "Scans", icon: WifiIcon },
  { to: "/assets", label: "Assets", icon: RectangleStackIcon },
  { to: "/findings", label: "Findings", icon: ExclamationTriangleIcon },
  { to: "/reports", label: "Reports", icon: DocumentChartBarIcon },
  { to: "/notifications", label: "Notifications", icon: BellIcon },
  { to: "/profile", label: "Profile", icon: UserCircleIcon },
  { to: "/settings", label: "Settings", icon: Cog6ToothIcon }
];

export function AppShell() {
  const [scanOpen, setScanOpen] = useState(false);
  const navigate = useNavigate();

  const logout = () => {
    localStorage.removeItem("sentinelasm.token");
    navigate("/login");
  };

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[280px_1fr]">
      <aside className="glass sticky top-0 z-30 h-screen border-y-0 border-l-0 p-5 max-lg:hidden">
        <div className="flex items-center gap-3">
          <div className="grid h-12 w-12 place-items-center rounded-2xl bg-cyan-400/10 ring-1 ring-cyan-300/30">
            <ShieldCheckIcon className="h-7 w-7 text-cyan-300" />
          </div>
          <div>
            <div className="text-lg font-bold">SentinelASM</div>
            <div className="text-xs uppercase tracking-[0.28em] text-cyan-200/70">Exposure Command</div>
          </div>
        </div>

        <button className="button-primary mt-8 w-full" onClick={() => setScanOpen(true)}>New Scan</button>
        <ScannerModeBadge />

        <nav className="mt-8 grid gap-2">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-2xl px-4 py-3 text-sm transition ${
                  isActive ? "bg-cyan-400/12 text-cyan-100 ring-1 ring-cyan-300/20" : "text-slate-400 hover:bg-white/5 hover:text-white"
                }`
              }
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <button onClick={logout} className="mt-8 flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-sm text-slate-400 hover:bg-white/5 hover:text-white">
          <ArrowRightOnRectangleIcon className="h-5 w-5" />
          Logout
        </button>
      </aside>

      <main className="min-w-0 px-5 py-5 lg:px-8">
        <header className="mb-6 flex items-center justify-between gap-4">
          <div>
            <p className="text-sm text-cyan-200/70">Authorized security assessment platform</p>
            <h1 className="text-2xl font-bold text-white md:text-3xl">Attack Surface Intelligence</h1>
          </div>
          <div className="hidden min-w-[320px] items-center gap-3 rounded-2xl border border-slate-700 bg-slate-950/50 px-4 py-3 md:flex">
            <MagnifyingGlassIcon className="h-5 w-5 text-slate-500" />
            <input className="w-full bg-transparent text-sm text-slate-200 outline-none" placeholder="Search domains, IPs, findings, ports..." />
          </div>
          <button className="button-primary lg:hidden" onClick={() => setScanOpen(true)}>New Scan</button>
        </header>
        <Outlet />
      </main>
      <NewScanModal open={scanOpen} onClose={() => setScanOpen(false)} />
    </div>
  );
}
