import { useQuery } from "@tanstack/react-query";
import { Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api } from "../api/client";
import type { DashboardStats } from "../api/types";
import { MetricCard } from "../components/MetricCard";

const fallback: DashboardStats = {
  totalDomains: 0,
  totalScans: 0,
  totalAssets: 0,
  aliveHosts: 0,
  criticalFindings: 0,
  highFindings: 0,
  mediumFindings: 0,
  lowFindings: 0,
  successRate: 0,
  monthlyTrend: [{ month: "Jun", assets: 0 }],
  severityDistribution: { critical: 0, high: 0, medium: 0, low: 0, informational: 0 },
  technologyDistribution: {},
  riskTrend: []
};

export function DashboardPage() {
  const { data = fallback, isLoading } = useQuery({
    queryKey: ["dashboard"],
    queryFn: async () => (await api.get<DashboardStats>("/api/dashboard")).data
  });

  const severity = Object.entries(data.severityDistribution).map(([name, value]) => ({ name, value }));
  const tech = Object.entries(data.technologyDistribution).map(([name, value]) => ({ name, value })).slice(0, 8);

  return (
    <div className="grid gap-6">
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <MetricCard label="Total Domains" value={data.totalDomains} />
        <MetricCard label="Total Scans" value={data.totalScans} />
        <MetricCard label="Total Assets" value={data.totalAssets} />
        <MetricCard label="Alive Hosts" value={data.aliveHosts} />
        <MetricCard label="Success Rate" value={`${data.successRate.toFixed(0)}%`} />
        <MetricCard label="Critical" value={data.criticalFindings} hint="Immediate review" />
        <MetricCard label="High" value={data.highFindings} />
        <MetricCard label="Medium" value={data.mediumFindings} />
        <MetricCard label="Low" value={data.lowFindings} />
        <MetricCard label="Last Scan" value={data.lastScanTime ? new Date(data.lastScanTime).toLocaleDateString() : "None"} />
      </section>

      {isLoading ? <div className="glass h-64 animate-pulse rounded-3xl" /> : null}

      <section className="grid gap-6 xl:grid-cols-3">
        <div className="glass rounded-3xl p-5 xl:col-span-2">
          <h2 className="font-semibold text-white">Asset Growth</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer>
              <AreaChart data={data.monthlyTrend}>
                <defs>
                  <linearGradient id="assets" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#22d3ee" stopOpacity={0.6} />
                    <stop offset="95%" stopColor="#22d3ee" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="month" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Area dataKey="assets" stroke="#22d3ee" fill="url(#assets)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="glass rounded-3xl p-5">
          <h2 className="font-semibold text-white">Severity Distribution</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer>
              <PieChart>
                <Pie data={severity} dataKey="value" nameKey="name" outerRadius={95}>
                  {severity.map((_, index) => <Cell key={index} fill={["#ef4444", "#f97316", "#f59e0b", "#10b981", "#38bdf8"][index]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <div className="glass rounded-3xl p-5">
          <h2 className="font-semibold text-white">Technology Distribution</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer>
              <BarChart data={tech}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="name" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Bar dataKey="value" fill="#3b82f6" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="glass rounded-3xl p-5">
          <h2 className="font-semibold text-white">Risk Trend</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer>
              <AreaChart data={data.riskTrend}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="date" stroke="#94a3b8" hide />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Area dataKey="risk" stroke="#60a5fa" fill="#2563eb44" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>
      </section>
    </div>
  );
}
