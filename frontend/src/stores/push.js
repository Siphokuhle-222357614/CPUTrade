import { defineStore } from "pinia";
import { getPushPublicKey, subscribePush, unsubscribePush } from "../api/push";

// The Push API wants the VAPID public key as a raw Uint8Array, but the
// backend hands it over as the same URL-safe base64 string it was generated
// as -- this is the standard conversion every web-push tutorial reaches for.
function urlBase64ToUint8Array(base64String) {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
  const rawData = window.atob(base64);
  return Uint8Array.from([...rawData].map((char) => char.charCodeAt(0)));
}

export const usePushStore = defineStore("push", {
  state: () => ({
    supported: typeof window !== "undefined" && "serviceWorker" in navigator && "PushManager" in window,
    subscribed: false,
    checked: false,
  }),

  getters: {
    // "default" = never asked; browsers give no way back from "denied" except the user's own site settings.
    permission: () => (typeof Notification !== "undefined" ? Notification.permission : "denied"),
  },

  actions: {
    async checkSubscription() {
      if (!this.supported) {
        this.checked = true;
        return;
      }
      try {
        const registration = await navigator.serviceWorker.ready;
        const subscription = await registration.pushManager.getSubscription();
        this.subscribed = !!subscription;
      } catch {
        // Leave `subscribed` as-is -- worst case the opt-in banner offers to enable again.
      } finally {
        this.checked = true;
      }
    },

    async enable() {
      if (!this.supported) return false;
      try {
        const permission = await Notification.requestPermission();
        if (permission !== "granted") return false;

        const registration = await navigator.serviceWorker.ready;
        const { data } = await getPushPublicKey();
        const subscription = await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: urlBase64ToUint8Array(data.publicKey),
        });
        await subscribePush(subscription.toJSON());
        this.subscribed = true;
        return true;
      } catch {
        return false;
      }
    },

    async disable() {
      if (!this.supported) return;
      try {
        const registration = await navigator.serviceWorker.ready;
        const subscription = await registration.pushManager.getSubscription();
        if (subscription) {
          await unsubscribePush(subscription.endpoint);
          await subscription.unsubscribe();
        }
      } catch {
        // Non-fatal -- the subscription still gets cleaned up server-side next time a push 404s/410s.
      } finally {
        this.subscribed = false;
      }
    },
  },
});
