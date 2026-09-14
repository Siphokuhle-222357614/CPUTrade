import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      // "autoUpdate" silently activates a new service worker (and reloads
      // the precache) on the next navigation once a new version is
      // deployed, instead of leaving a student stuck on a stale build until
      // they manually clear the cache.
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg', 'icons/apple-touch-icon.png'],
      manifest: {
        name: 'CPUTrade',
        short_name: 'CPUTrade',
        description: 'The CPUT student marketplace — buy, sell and swap with fellow students.',
        // Matches the "Trust Blue & Teal" design system (styles/tokens.css):
        // theme_color tints the OS status bar/title bar, background_color is
        // the splash-screen background while the app boots.
        theme_color: '#16305c',
        background_color: '#f5f7fa',
        display: 'standalone',
        start_url: '/',
        scope: '/',
        orientation: 'portrait-primary',
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png', purpose: 'any' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png', purpose: 'any' },
          // "maskable" variants let Android crop/reshape the icon (circle,
          // squircle, teardrop, ...) per the device's icon theme instead of
          // stamping our square PNG inside another shape with an ugly seam.
          { src: '/icons/icon-maskable-192.png', sizes: '192x192', type: 'image/png', purpose: 'maskable' },
          { src: '/icons/icon-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Only precache the built app shell (JS/CSS/HTML/icons) — never the
        // API. Runtime requests to /api/** are routed below with an explicit,
        // conservative strategy so a stale cache can never serve someone
        // else's chat messages or a suspended account a stale "active" status.
        globPatterns: ['**/*.{js,css,html,svg,png,ico,woff2}'],
        navigateFallbackDenylist: [/^\/api\//],
        runtimeCaching: [
          {
            // Product listings/images are safe to show briefly stale while a
            // fresh copy loads in the background -- keeps browsing snappy on
            // poor campus wifi/mobile data without ever going fully offline
            // for a logged-in, personalized view.
            urlPattern: ({ url, request }) =>
              request.method === 'GET' && /\/api\/products(\/|$|\?)/.test(url.pathname + url.search),
            handler: 'StaleWhileRevalidate',
            options: {
              cacheName: 'cputrade-products',
              expiration: { maxEntries: 200, maxAgeSeconds: 60 * 60 },
            },
          },
        ],
      },
    }),
  ],
})
