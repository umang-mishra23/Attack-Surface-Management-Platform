import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import type { ScanSummary } from "../api/types";

export function ReportsPage() {
  const { data = [] } = useQuery({ queryKey: ["scans"], queryFn: async () => (await api.get<ScanSummary[]>("/api/scans")).data });
  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="text-xl font-bold">Enterprise Reports</h2>
      <p className="mt-2 text-sm text-slate-400">Export executive-ready evidence packages as JSON or CSV. PDF/Excel hooks are reserved in the API layer.</p>
      <div className="mt-5 grid gap-3">
        {data.map((scan) => (
          <div key={scan.id} className="flex items-center justify-between rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div><strong>{scan.domain}</strong><p className="text-sm text-slate-400">Risk score {scan.riskScore}</p></div>
            <div className="flex gap-2">
              <a className="button-muted" href={`/api/reports/${scan.id}.json`}>JSON</a>
              <a className="button-muted" href={`/api/reports/${scan.id}.csv`}>CSV</a>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
