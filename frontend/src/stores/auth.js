import { defineStore } from "pinia";
import * as authApi from "../api/auth";

function loadStoredUser() {
  try {
    const raw = localStorage.getItem("cputrade_user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("cputrade_token") || null,
    user: loadStoredUser(),
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === "ADMIN",
  },

  actions: {
    async login(credentials) {
      const { data } = await authApi.login(credentials);
      this.token = data.token;
      this.user = data.user;
      localStorage.setItem("cputrade_token", data.token);
      localStorage.setItem("cputrade_user", JSON.stringify(data.user));
      return data.user;
    },

    async register(payload) {
      // Registration doesn't log the user in automatically — they sign in
      // afterwards (mirrors US1.3/US1.5: a vendor may need approval first).
      const { data } = await authApi.register(payload);
      return data;
    },

    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem("cputrade_token");
      localStorage.removeItem("cputrade_user");
    },
  },
});
