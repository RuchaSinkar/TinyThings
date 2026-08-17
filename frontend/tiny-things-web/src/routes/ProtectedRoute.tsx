import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../auth/authStore';

export function ProtectedRoute({ children }: { children: JSX.Element }) {
  const isAuthed = useAuthStore((s) => !!s.accessToken);
  return isAuthed ? children : <Navigate to="/login" replace />;
}