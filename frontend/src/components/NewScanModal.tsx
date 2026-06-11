import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import { api } from "../api/client";

export function NewScanModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const queryClient = useQueryClient();
  const [domain, setDomain] = useState("");
  const [scanType, setScanType] = useState("STANDARD");
  const [priority, setPriority] = useState("NORMAL");
  const [authorized, setAuthorized] = useState(false);

  const createScan = useMutation({
    mutationFn: async () => api.post("/api/scans", { domain, scanType, priority, authorized }),
    onSuccess: () => {
      toast.success("Scan job queued");
      queryClient.invalidateQueries({ queryKey: ["scans"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      setDomain("");
      setScanType("STANDARD");
      setPriority("NORMAL");
      setAuthorized(false);
      onClose();
    },
    onError: (error: unknown) => {
      const message = error && typeof error === "object" && "response" in error
        ? (error as { response?: { data?: { error?: string } } }).response?.data?.error
        : undefined;
      toast.error(message || "Unable to start scan");
    }
  });

  const submit = (event: FormEvent) => {
    event.preventDefault();
    createScan.mutate();
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/80 p-4 backdrop-blur-sm" role="dialog" aria-modal="true">
      <button className="absolute inset-0 cursor-default" aria-label="Close modal" onClick={onClose} />
      <form onSubmit={submit} className="glass relative w-full max-w-xl rounded-3xl p-6">
          <h2 className="text-xl font-bold">New Scan</h2>
          <p className="mt-2 text-sm text-slate-400">Create an asynchronous assessment job for an authorized domain.</p>
          <div className="mt-6 grid gap-4">
            <input className="input" placeholder="example.com" value={domain} onChange={(event) => setDomain(event.target.value)} autoFocus />
            <div className="grid grid-cols-2 gap-3">
              <select className="input" value={scanType} onChange={(event) => setScanType(event.target.value)}>
                <option value="QUICK">Quick</option>
                <option value="STANDARD">Standard</option>
                <option value="DEEP">Deep</option>
              </select>
              <select className="input" value={priority} onChange={(event) => setPriority(event.target.value)}>
                <option value="LOW">Low</option>
                <option value="NORMAL">Normal</option>
                <option value="HIGH">High</option>
              </select>
            </div>
            <label className="flex items-center gap-3 text-sm text-slate-300">
              <input type="checkbox" checked={authorized} onChange={(event) => setAuthorized(event.target.checked)} />
              I am authorized to assess this domain.
            </label>
          </div>
          <div className="mt-6 flex justify-end gap-3">
            <button type="button" className="button-muted" onClick={onClose}>Cancel</button>
            <button type="submit" className="button-primary" disabled={createScan.isPending}>
              {createScan.isPending ? "Queuing..." : "Start Scan"}
            </button>
          </div>
      </form>
    </div>
  );
}
