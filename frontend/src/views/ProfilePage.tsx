import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import type { UserProfile } from "../api/types";

export function ProfilePage() {
  const { data } = useQuery({ queryKey: ["me"], queryFn: async () => (await api.get<UserProfile>("/api/auth/me")).data });
  return (
    <div className="grid gap-6 xl:grid-cols-[360px_1fr]">
      <section className="glass rounded-3xl p-5">
        <div className="grid h-24 w-24 place-items-center rounded-3xl bg-cyan-400/10 text-3xl font-bold text-cyan-200">{data?.name?.[0] ?? "S"}</div>
        <h2 className="mt-4 text-xl font-bold">{data?.name}</h2>
        <p className="text-slate-400">{data?.email}</p>
        <p className="mt-2 text-sm text-cyan-200">{data?.company}</p>
      </section>
      <section className="glass rounded-3xl p-5">
        <h2 className="text-xl font-bold">Security Profile</h2>
        <div className="mt-5 grid gap-4">
          <input className="input" value={data?.apiKey ?? ""} readOnly />
          <input className="input" placeholder="Current password" type="password" />
          <input className="input" placeholder="New password" type="password" />
          <button className="button-primary w-fit">Change Password</button>
        </div>
      </section>
    </div>
  );
}
