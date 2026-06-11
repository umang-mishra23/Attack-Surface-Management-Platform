import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import type { Finding } from "../api/types";
import { SeverityBadge } from "../components/SeverityBadge";

export function FindingsPage() {
  const { data = [] } = useQuery({ queryKey: ["findings"], queryFn: async () => (await api.get<Finding[]>("/api/findings")).data });
  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="text-xl font-bold">Vulnerability Management</h2>
      <div className="mt-5 grid gap-4">
        {data.map((finding) => (
          <article key={finding.id} className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div><h3 className="font-semibold text-white">{finding.name}</h3><p className="text-sm text-slate-400">{finding.affectedUrl}</p></div>
              <SeverityBadge severity={finding.severity} />
            </div>
            <p className="mt-3 text-sm text-slate-300">{finding.description}</p>
            <p className="mt-3 text-sm text-cyan-100">{finding.recommendation}</p>
          </article>
        ))}
      </div>
    </div>
  );
}
