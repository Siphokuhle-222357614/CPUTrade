// Custom service worker (vite-plugin-pwa "injectManifest" strategy) instead
// of the plugin's auto-generated one — a generated Workbox SW has no place to
// hang a `push`/`notificationclick` listener, and real Web Push is the whole
// reason this file exists instead of the default.
import { createHandlerBoundToURL, precacheAndRoute } from "workbox-precaching";
import { NavigationRoute, registerRoute } from "workbox-routing";
import { StaleWhileRevalidate } from "workbox-strategies";
import { ExpirationPlugin } from "workbox-expiration";

// Injected by vite-plugin-pwa at build time with the app-shell file list —
// empty in dev (devOptions doesn't precache anything, it just registers this
// SW so push/notificationclick can be exercised under `npm run dev`).
const precacheManifest = self.__WB_MANIFEST;
precacheAndRoute(precacheManifest);

// SPA offline fallback: any navigation not otherwise handled falls back to
// the cached index.html so client-side routing still works offline, except
// /api/** (which was never a navigation target anyway, but mirrors the
// denylist the previous generateSW config had for the same reason).
// Guarded on a non-empty manifest -- createHandlerBoundToURL throws
// synchronously (crashing the whole SW's evaluation) if "index.html" isn't
// actually a precache entry, which is exactly the case in dev.
if (precacheManifest.length > 0) {
  registerRoute(new NavigationRoute(createHandlerBoundToURL("index.html"), { denylist: [/^\/api\//] }));
}

// Product listings/images are safe to show briefly stale while a fresh copy
// loads in the background — keeps browsing snappy on poor campus wifi/mobile
// data without ever going fully offline for a logged-in, personalized view.
// Everything else (auth, chat, admin, wishlist, push) is deliberately left
// un-cached so a stale response can never show someone a suspended account
// as active or someone else's chat thread.
registerRoute(
  ({ url, request }) => request.method === "GET" && /\/api\/products(\/|$|\?)/.test(url.pathname + url.search),
  new StaleWhileRevalidate({
    cacheName: "cputrade-products",
    plugins: [new ExpirationPlugin({ maxEntries: 200, maxAgeSeconds: 60 * 60 })],
  })
);

self.addEventListener("install", () => self.skipWaiting());
self.addEventListener("activate", (event) => event.waitUntil(self.clients.claim()));

// Shows the notification the backend's WebPushService sent, whether or not
// the app is open in a tab right now — the entire point of push over the
// in-app 20s poll, which only ever runs while a tab is actually open.
self.addEventListener("push", (event) => {
  if (!event.data) return;
  let payload;
  try {
    payload = event.data.json();
  } catch {
    return;
  }
  const { title, body, link } = payload;
  event.waitUntil(
    self.registration.showNotification(title || "CPUTrade", {
      body,
      icon: "/icons/icon-192.png",
      badge: "/icons/icon-192.png",
      data: { link: link || "/" },
    })
  );
});

// Focuses an already-open CPUTrade tab and navigates it, rather than always
// opening a new one — a student clicking a chat notification wants their
// existing tab to jump to it, not a second copy of the app.
self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  const link = event.notification.data?.link || "/";
  event.waitUntil(
    self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((clients) => {
      for (const client of clients) {
        if ("focus" in client) {
          client.postMessage({ type: "navigate", link });
          return client.focus();
        }
      }
      return self.clients.openWindow(link);
    })
  );
});
