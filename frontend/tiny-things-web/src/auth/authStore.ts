import { create } from 'zustand';
import axios from 'axios';

interface AuthState {
  accessToken: string | null;
  setAccessToken: (token: string | null) => void;
  tryRefresh: () => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,

  setAccessToken: (token) => set({ accessToken: token }),

  tryRefresh: async () => {
    try {
      const res = await axios.post(
        `${import.meta.env.VITE_API_URL}/api/auth/refresh`,
        {},
        { withCredentials: true }
      );
      set({ accessToken: res.data.accessToken });
    } catch {
      set({ accessToken: null });
    }
  },

  logout: async () => {
    await axios.post(
      `${import.meta.env.VITE_API_URL}/api/auth/logout`,
      {},
      { withCredentials: true }
    );
    set({ accessToken: null });
  },
}));