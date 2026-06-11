import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { api } from "../api/client";
import { AuthLayout } from "./AuthLayout";

export function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const { data } = await api.post("/api/auth/login", { email, password });
      localStorage.setItem("sentinelasm.token", data.token);
      navigate("/");
    } catch {
      toast.error("Invalid credentials");
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to your attack surface command center.">
      <form onSubmit={submit} className="grid gap-4">
        <input className="input" placeholder="Email" value={email} onChange={(event) => setEmail(event.target.value)} />
        <input className="input" placeholder="Password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
        <button className="button-primary">Login</button>
        <div className="flex justify-between text-sm text-slate-400">
          <Link to="/register" className="text-cyan-200">Create account</Link>
          <span>Forgot password?</span>
        </div>
      </form>
    </AuthLayout>
  );
}
