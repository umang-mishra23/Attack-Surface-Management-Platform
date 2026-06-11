import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import type { Notification } from "../api/types";

export function NotificationsPage() {
  const { data = [] } = useQuery({ queryKey: ["notifications"], queryFn: async () => (await api.get<Notification[]>("/api/notifications")).data });
  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="text-xl font-bold">Notifications</h2>
      <div className="mt-5 grid gap-3">
        {data.map((item) => <div key={item.id} className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4"><strong>{item.title}</strong><p className="text-sm text-slate-400">{item.message}</p></div>)}
      </div>
    </div>
  );
}
