import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import type { ScanSummary } from "../api/types";

export function ScansPage() {
  const { data = [] } = useQuery({ queryKey: ["scans"], queryFn: async () => (await api.get<ScanSummary[]>("/api/scans")).data });

  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="text-xl font-bold">Scan History</h2>
      <div className="mt-5 overflow-hidden rounded-2xl border border-slate-800">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-950/70 text-slate-400">
            <tr><th className="p-4">Domain</th><th>Status</th><th>Progress</th><th>Risk</th><th>Created</th></tr>
          </thead>
          <tbody>
            {data.map((scan) => (
              <tr key={scan.id} className="border-t border-slate-800 hover:bg-white/[0.03]">
                <td className="p-4"><Link className="text-cyan-200" to={`/scans/${scan.id}`}>{scan.domain}</Link></td>
                <td>{scan.status}</td>
                <td><div className="h-2 w-32 rounded-full bg-slate-800"><div className="h-2 rounded-full bg-cyan-400" style={{ width: `${scan.progress}%` }} /></div></td>
                <td>{scan.riskScore}</td>
                <td>{new Date(scan.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
