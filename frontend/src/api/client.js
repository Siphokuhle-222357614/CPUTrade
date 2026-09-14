import axios from "axios";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// Attach the JWT (if we have one) to every request.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("cputrade_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// A 401 means the token is missing/expired — clear it and bounce to login.
// (This is a UX convenience; the real enforcement is always server-side.)
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("cputrade_token");
      localStorage.removeItem("cputrade_user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default client;
