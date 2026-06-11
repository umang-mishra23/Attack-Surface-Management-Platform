import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import type { Asset } from "../api/types";

export function AssetsPage() {
  const { data = [] } = useQuery({ queryKey: ["assets"], queryFn: async () => (await api.get<Asset[]>("/api/assets")).data });
  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="text-xl font-bold">Asset Inventory</h2>
      <div className="mt-4 grid gap-3 md:grid-cols-3">
        <input className="input" placeholder="Search assets" />
        <select className="input"><option>All statuses</option><option>Alive</option></select>
        <button className="button-muted">Export CSV</button>
      </div>
      <div className="mt-5 overflow-hidden rounded-2xl border border-slate-800">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-950/70 text-slate-400"><tr><th className="p-4">Subdomain</th><th>IP</th><th>Status</th><th>Code</th><th>Technologies</th><th>Risk</th></tr></thead>
          <tbody>{data.map((asset) => <tr key={asset.id} className="border-t border-slate-800"><td className="p-4 text-cyan-200">{asset.subdomain}</td><td>{asset.ipAddress}</td><td>{asset.status}</td><td>{asset.responseCode ?? "-"}</td><td>{asset.technologies?.join(", ")}</td><td>{asset.riskScore}</td></tr>)}</tbody>
        </table>
      </div>
    </div>
  );
}
