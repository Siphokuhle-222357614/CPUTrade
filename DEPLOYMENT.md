# Deploying CPUTrade

This app has three independent pieces to stand up: **MySQL**, the **Spring
Boot API**, and the **Vue static frontend**. None of them need Docker — the
same `mvnw`/`npm` commands used for local dev produce what you deploy; the
only difference is which environment variables point at production values
instead of the local defaults baked into the repo.

Where you host each piece is up to you (a VPS, Render/Railway for the API,
Netlify/Vercel/any static host for the frontend, a managed MySQL instance,
...) — this guide covers what each piece needs, not a specific provider.

## 1. Required environment variables (backend)

None of these have safe defaults for anywhere but localhost. The app will
still boot without them (falling back to the dev-only values in
`application.properties`), but `JwtUtil` logs a loud warning at startup if
`JWT_SECRET` is missing, precisely so this can't fail silently.

| Variable | Purpose | Example |
|---|---|---|
| `JWT_SECRET` | Signs/verifies login tokens. Generate a real one — never reuse the value committed in this repo. | `openssl rand -base64 48` |
| `DB_URL` | JDBC URL of your production MySQL. | `jdbc:mysql://db-host:3306/cputrade?useSSL=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL app user (not root). | `cputrade_app` |
| `DB_PASSWORD` | That user's password. | — |
| `CORS_ALLOWED_ORIGIN` | The exact origin your deployed frontend is served from (no trailing slash). | `https://cputrade.example.com` |
| `PORT` | Only needed if your host assigns one (Render/Railway/Heroku do this automatically). | `10000` |

Set these however your host expects (platform dashboard, `systemd`
`Environment=` lines, a `.env` file read by your process manager — Spring
Boot itself doesn't read `.env` files, so if you use one, load it into the
shell environment before starting the jar).

## 2. Database

Run `backend/db/setup.sql` (as root, once) against your production MySQL to
create the `cputrade` database and the `cputrade_app` user — edit the
password in that file first, and use the same value for `DB_PASSWORD` above.
Full steps, including the first-boot-then-seed-admin sequence, are in
`backend/README.md`; they're identical for production, just pointed at a
different host.

`spring.jpa.hibernate.ddl-auto=update` is still in effect — convenient for
this project's scale, but if this app ever gets a second environment (staging
+ prod) or a second developer, switch to Flyway/Liquibase migrations first so
schema changes are reviewable and repeatable instead of inferred by Hibernate
on boot.

## 3. Backend

```bash
cd backend
./mvnw.cmd clean package -DskipTests   # produces target/*.jar
# on the server, with the env vars from Section 1 set:
java -jar target/cputrade-backend-*.jar
```

Put this behind a process manager (systemd, pm2, your host's own supervisor)
so it restarts on crash/reboot, and behind a reverse proxy (nginx, Caddy, or
your host's built-in TLS termination) so it's served over HTTPS — the
frontend and backend are on different origins, so the browser will refuse
mixed-content or CORS-mismatched requests otherwise.

Confirm it's up: `curl https://your-backend-domain/api/products` should
return `[]` or your seeded listings, not a connection error.

## 4. Frontend

Vite only reads `.env.production` (not `.env.development`) for `npm run
build`. Before building for anywhere but localhost:

```bash
cd frontend
cp .env.production.example .env.production
# edit VITE_API_BASE_URL in that file to your deployed backend's /api URL
npm install
npm run build       # outputs frontend/dist/
```

`dist/` is a static site — upload it to any static host, or serve it with
nginx. Since this is a client-side-routed SPA (Vue Router with
`createWebHistory`), your static host needs a fallback rule that serves
`index.html` for any path that isn't a real file (Netlify/Vercel do this
automatically; nginx needs `try_files $uri /index.html;`).

## 5. First admin account

There's no self-registration path to ADMIN by design (see
`backend/README.md`). Seed one the same way as local dev:
`backend/db/seed-admin.sql`, run once against production after the app's
first boot has created the `users` table — change that file's password hash
first (it currently seeds a documented dev password).

## Known limitations (deliberately out of scope so far)

- **No login rate-limiting** — repeated failed `/api/auth/login` attempts
  aren't throttled. Fine for a class project's traffic; add one (e.g.
  Bucket4j, or your reverse proxy's own rate limiting) before this handles
  real, unknown users.
- **No real email** — `EmailNotifier`'s only implementation
  (`NoOpEmailNotifier`) logs instead of sending. Swap in a real provider
  (EmailJS, SMTP, SES) by adding a second `EmailNotifier` implementation and
  marking it `@Primary` — nothing else in the codebase needs to change.
- **No automated tests** — verification so far has been manual curl smoke
  tests per story plus `npm run build`/`mvnw compile`. Worth adding before
  any further changes, especially before onboarding a second contributor.
