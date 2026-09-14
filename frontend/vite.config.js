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
      // A hand-written service worker (src/sw.js) instead of the plugin's
      // generated one -- real Web Push needs a `push`/`notificationclick`
      // listener, which a generated Workbox SW has no hook for.
      strategies: 'injectManifest',
      srcDir: 'src',
      filename: 'sw.js',
      injectManifest: {
        globPatterns: ['**/*.{js,css,html,svg,png,ico,woff2}'],
      },
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
      // Precaching + the /api/products runtime-caching strategy both moved
      // into src/sw.js, since injectManifest doesn't take a `workbox` block.
      // Enabled in dev too (not just the production build) -- Web Push needs
      // an actually-registered service worker to subscribe against, and that's
      // exactly what's running under `npm run dev` while this gets tested.
      devOptions: {
        enabled: true,
        type: 'module',
      },
    }),
  ],
})
