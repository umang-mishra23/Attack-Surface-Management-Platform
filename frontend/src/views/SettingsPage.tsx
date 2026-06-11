export function SettingsPage() {
  return (
    <div className="glass rounded-3xl p-5">
      <h2 className="text-xl font-bold">Settings</h2>
      <div className="mt-5 grid gap-4 md:grid-cols-2">
        <select className="input"><option>Dark theme</option></select>
        <select className="input"><option>Default scan: Standard</option><option>Quick</option><option>Deep</option></select>
        <select className="input"><option>Timezone: Local</option></select>
        <select className="input"><option>Language: English</option></select>
      </div>
      <div className="mt-6 grid gap-3">
        <input className="input" placeholder="Slack webhook URL" />
        <input className="input" placeholder="Discord webhook URL" />
        <input className="input" placeholder="Telegram chat ID" />
        <button className="button-primary w-fit">Save Settings</button>
      </div>
    </div>
  );
}
