# CPUTrade Frontend

Vue 3 + Vite single-page app (Vue Router, Pinia, Axios), installable as a PWA
with offline caching and Web Push.

```bash
npm install
npm run dev      # http://localhost:5173 (must be 5173, or the backend's CORS blocks it)
npm run build    # production build into dist/ (needs .env.production, see DEPLOYMENT.md)
```

The backend URL comes from `VITE_API_BASE_URL` in `.env.development`
(default `http://localhost:8080/api`).

Full setup, including the database and backend, is in the
[main README](../README.md#5-run-it-locally--overview).
