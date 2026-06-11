import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { api } from "../api/client";
import { AuthLayout } from "./AuthLayout";

export function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", company: "" });

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const { data } = await api.post("/api/auth/register", form);
      localStorage.setItem("sentinelasm.token", data.token);
      navigate("/");
    } catch {
      toast.error("Registration failed");
    }
  };

  return (
    <AuthLayout title="Create workspace" subtitle="Launch a polished ASM workspace for your team.">
      <form onSubmit={submit} className="grid gap-4">
        {(["name", "email", "company"] as const).map((key) => (
          <input key={key} className="input" placeholder={key[0].toUpperCase() + key.slice(1)} value={form[key]} onChange={(event) => setForm({ ...form, [key]: event.target.value })} />
        ))}
        <input className="input" placeholder="Password" type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
        <button className="button-primary">Register</button>
        <Link to="/login" className="text-sm text-cyan-200">Already have an account?</Link>
      </form>
    </AuthLayout>
  );
}
