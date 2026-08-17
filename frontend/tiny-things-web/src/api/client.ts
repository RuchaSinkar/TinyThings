import axios from 'axios';
import { useAuthStore } from '../auth/authStore';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true, // needed so the refresh_token cookie is sent
});

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let isRefreshing = false;

client.interceptors.response.use(
  (res) => res,
  async (err) => {
    const original = err.config;

    if (err.response?.status === 401 && !original._retry && !isRefreshing) {
      original._retry = true;
      isRefreshing = true;
      try {
        await useAuthStore.getState().tryRefresh();
        isRefreshing = false;
        const newToken = useAuthStore.getState().accessToken;
        if (newToken) {
          original.headers.Authorization = `Bearer ${newToken}`;
          return client(original);
        }
      } catch {
        isRefreshing = false;
      }
    }

    return Promise.reject(err);
  }
);

export default client;