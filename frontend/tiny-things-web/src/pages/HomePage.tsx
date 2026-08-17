import { useAuthStore } from '../auth/authStore';

export function HomePage() {
  const logout = useAuthStore((s) => s.logout);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-900">
      <div className="text-center text-white">
        <h1 className="text-2xl font-semibold">You're in 🎉</h1>
        <p className="mt-2 text-slate-400">This is the Home Page — Tiny Things dashboard goes here.</p>
        <button
          onClick={logout}
          className="mt-6 rounded bg-slate-700 px-4 py-2 hover:bg-slate-600"
        >
          Log out
        </button>
      </div>
    </div>
  );
}