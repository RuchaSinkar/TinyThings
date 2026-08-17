import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import client from '../api/client';
import { useAuthStore } from './authStore';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const setAccessToken = useAuthStore((s) => s.setAccessToken);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const res = await client.post('/api/auth/login', { email, password });
      setAccessToken(res.data.accessToken);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Login failed');
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-900">
      <form onSubmit={handleSubmit} className="w-80 space-y-4 rounded-lg bg-slate-800 p-6">
        <h1 className="text-xl font-semibold text-white">Welcome back</h1>

        {error && <p className="text-sm text-red-400">{error}</p>}

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          className="w-full rounded bg-slate-700 p-2 text-white placeholder-slate-400"
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          className="w-full rounded bg-slate-700 p-2 text-white placeholder-slate-400"
        />

        <button
          type="submit"
          className="w-full rounded bg-indigo-500 p-2 font-medium text-white hover:bg-indigo-600"
        >
          Log in
        </button>

        <p className="text-sm text-slate-400">
          No account yet? <Link to="/signup" className="text-indigo-400">Sign up</Link>
        </p>
      </form>
    </div>
  );
}