import { motion } from "framer-motion";

export function MetricCard({ label, value, hint }: { label: string; value: string | number; hint?: string }) {
  return (
    <motion.div
      className="metric-card"
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
    >
      <p className="text-sm text-slate-400">{label}</p>
      <div className="mt-3 text-3xl font-bold text-white">{value}</div>
      {hint && <p className="mt-2 text-xs text-cyan-200/80">{hint}</p>}
    </motion.div>
  );
}
