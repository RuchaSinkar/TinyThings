import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { LoginPage } from './auth/LoginPage';
import { SignupPage } from './auth/SignupPage';
import { HomePage } from './pages/HomePage';
import { OnboardingFlow } from './onboarding/OnboardingFlow';
import { ProtectedRoute } from './routes/ProtectedRoute';

export default function App() {
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