import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";

interface ScannerStatus {
  mode: string;
  realToolsEnabled: boolean;
  submergeExists: boolean;
}

export function ScannerModeBadge() {
  const { data } = useQuery({
    queryKey: ["scanner-status"],
    queryFn: async () => (await api.get<ScannerStatus>("/api/system/scanner")).data,
    retry: false
  });

  const real = data?.realToolsEnabled;
  const label = real ? "Real Tools" : "Demo Mode";
  const pathState = real && !data?.submergeExists ? "SubMerge missing" : data?.mode ?? "unknown";

  return (
    <div className={`mt-4 rounded-2xl border px-4 py-3 text-xs ${real ? "border-emerald-400/30 bg-emerald-400/10 text-emerald-100" : "border-amber-400/30 bg-amber-400/10 text-amber-100"}`}>
      <div className="font-bold">{label}</div>
      <div className="mt-1 text-white/70">{pathState}</div>
    </div>
  );
}
