import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { LoginPage } from './auth/LoginPage';
import { SignupPage } from './auth/SignupPage';
import { HomePage } from './pages/HomePage';
import { OnboardingFlow } from './onboarding/OnboardingFlow';
import { SettingsPage } from './settings/SettingsPage';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { useAuthStore } from './auth/authStore';

export default function App() {
  const tryRefresh = useAuthStore((s) => s.tryRefresh);
  const [checkedAuth, setCheckedAuth] = useState(false);

  useEffect(() => {
    tryRefresh().finally(() => setCheckedAuth(true));
  }, []);

  if (!checkedAuth) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-900">
        <p className="text-slate-400">Loading...</p>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route
          path="/onboarding"
          element={
            <ProtectedRoute>
              <OnboardingFlow />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <SettingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}