import { defineStore } from "pinia";

// Toasts are for "your action just succeeded/failed" confirmations that
// otherwise had no feedback at all (e.g. creating a listing silently
// redirected you with nothing to show it worked) -- NOT a replacement for
// inline form-validation errors, which stay next to the field they concern.
let nextId = 1;

export const useToastStore = defineStore("toast", {
  state: () => ({
    toasts: [], // { id, type: "success" | "error" | "info", message }
  }),

  actions: {
    show(message, type = "info", duration = 4000) {
      const id = nextId++;
      this.toasts.push({ id, type, message });
      setTimeout(() => this.dismiss(id), duration);
      return id;
    },
    success(message, duration) {
      return this.show(message, "success", duration);
    },
    error(message, duration = 5500) {
      return this.show(message, "error", duration);
    },
    info(message, duration) {
      return this.show(message, "info", duration);
    },
    dismiss(id) {
      this.toasts = this.toasts.filter((toast) => toast.id !== id);
    },
  },
});
