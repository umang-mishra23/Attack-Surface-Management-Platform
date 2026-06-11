import { ShieldCheckIcon } from "@heroicons/react/24/outline";

export function AuthLayout({ title, subtitle, children }: { title: string; subtitle: string; children: React.ReactNode }) {
  return (
    <div className="grid min-h-screen place-items-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto grid h-16 w-16 place-items-center rounded-3xl bg-cyan-400/10 ring-1 ring-cyan-300/30">
            <ShieldCheckIcon className="h-9 w-9 text-cyan-300" />
          </div>
          <h1 className="mt-5 text-3xl font-bold text-white">{title}</h1>
          <p className="mt-2 text-sm text-slate-400">{subtitle}</p>
        </div>
        <div className="glass rounded-3xl p-6">{children}</div>
      </div>
    </div>
  );
}
