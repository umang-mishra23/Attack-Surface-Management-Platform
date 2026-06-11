import type { Severity } from "../api/types";

const styles: Record<Severity, string> = {
  CRITICAL: "bg-red-500/15 text-red-200 ring-1 ring-red-400/30",
  HIGH: "bg-orange-500/15 text-orange-200 ring-1 ring-orange-400/30",
  MEDIUM: "bg-amber-500/15 text-amber-200 ring-1 ring-amber-400/30",
  LOW: "bg-emerald-500/15 text-emerald-200 ring-1 ring-emerald-400/30",
  INFORMATIONAL: "bg-sky-500/15 text-sky-200 ring-1 ring-sky-400/30"
};

export function SeverityBadge({ severity }: { severity: Severity }) {
  return <span className={`badge ${styles[severity]}`}>{severity}</span>;
}
