import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppShell } from "./shell/AppShell";
import { LoginPage } from "./views/LoginPage";
import { RegisterPage } from "./views/RegisterPage";
import { DashboardPage } from "./views/DashboardPage";
import { ScansPage } from "./views/ScansPage";
import { AssetsPage } from "./views/AssetsPage";
import { FindingsPage } from "./views/FindingsPage";
import { ReportsPage } from "./views/ReportsPage";
import { NotificationsPage } from "./views/NotificationsPage";
import { ProfilePage } from "./views/ProfilePage";
import { SettingsPage } from "./views/SettingsPage";
import { ScanDetailPage } from "./views/ScanDetailPage";

const isAuthed = () => Boolean(localStorage.getItem("sentinelasm.token"));

const Guard = ({ children }: { children: JSX.Element }) => {
  return isAuthed() ? children : <Navigate to="/login" replace />;
};

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  {
    path: "/",
    element: (
      <Guard>
        <AppShell />
      </Guard>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: "scans", element: <ScansPage /> },
      { path: "scans/:id", element: <ScanDetailPage /> },
      { path: "assets", element: <AssetsPage /> },
      { path: "findings", element: <FindingsPage /> },
      { path: "reports", element: <ReportsPage /> },
      { path: "notifications", element: <NotificationsPage /> },
      { path: "profile", element: <ProfilePage /> },
      { path: "settings", element: <SettingsPage /> }
    ]
  }
]);
