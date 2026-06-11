import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";

export function ScanDetailPage() {
  const { id } = useParams();
  const [liveLogs, setLiveLogs] = useState<string[]>([]);
  const [liveStatus, setLiveStatus] = useState<string | null>(null);
  const [liveProgress, setLiveProgress] = useState<number | null>(null);
  const { data } = useQuery({ queryKey: ["scan", id], queryFn: async () => (await api.get(`/api/scans/${id}`)).data, refetchInterval: 5000 });

  useEffect(() => {
    const token = localStorage.getItem("sentinelasm.token");
    if (!id || !token) return;
    const source = new EventSource(`/api/scans/${id}/events?token=${encodeURIComponent(token)}`);
    source.addEventListener("status", (event) => {
      const payload = JSON.parse((event as MessageEvent).data);
      setLiveStatus(payload.status);
      setLiveProgress(payload.progress);
      setLiveLogs((current) => [...current, ...(payload.logs ?? [])]);
      if (["COMPLETED", "FAILED", "CANCELLED"].includes(payload.status)) source.close();
    });
    source.onerror = () => source.close();
    return () => source.close();
  }, [id]);

  const logs = liveLogs.length ? liveLogs : data?.logs ?? [];
  const status = liveStatus ?? data?.summary?.status;
  const progress = liveProgress ?? data?.summary?.progress;

  return (
    <div className="grid gap-6 xl:grid-cols-[1fr_420px]">
      <section className="glass rounded-3xl p-5">
        <h2 className="text-xl font-bold">{data?.summary?.domain ?? "Scan Detail"}</h2>
          <div className="mt-4 grid gap-4 md:grid-cols-4">
          <div className="metric-card"><p className="text-slate-400">Status</p><strong>{status}</strong></div>
          <div className="metric-card"><p className="text-slate-400">Progress</p><strong>{progress}%</strong></div>
          <div className="metric-card"><p className="text-slate-400">Risk</p><strong>{data?.summary?.riskScore}</strong></div>
          <div className="metric-card"><p className="text-slate-400">Type</p><strong>{data?.summary?.scanType}</strong></div>
        </div>
        <div className="mt-6 rounded-2xl border border-slate-800 bg-slate-950/50 p-4">
          <h3 className="font-semibold">Live Scan Logs</h3>
          <pre className="mt-4 max-h-[420px] overflow-auto text-sm leading-7 text-cyan-100">{logs.join("\n")}</pre>
        </div>
      </section>
      <section className="glass rounded-3xl p-5">
        <h2 className="text-xl font-bold">Pipeline</h2>
        {["SubMerge", "HTTPX", "Nmap", "WhatWeb", "Nuclei", "Risk Calculation", "Report"].map((step) => (
          <div key={step} className="mt-4 flex items-center gap-3">
            <div className="h-3 w-3 rounded-full bg-cyan-300 shadow-glow" />
            <span className="text-slate-300">{step}</span>
          </div>
        ))}
      </section>
    </div>
  );
}
