# CPUTrade Backend

Spring Boot 4.1.1 (Java 21) REST API — JWT auth, product listings, admin
moderation. Covers the must-have user stories only (see the root README).

## 1. Prerequisites

- JDK 21 (Temurin recommended)
- MySQL 8.x running locally
- No separate Maven install needed — use the bundled wrapper (`./mvnw.cmd` on
  Windows, `./mvnw` on macOS/Linux)

## 2. Database setup (once)

Run this against your local MySQL as an admin user (e.g. `root`):

```sql
CREATE DATABASE cputrade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cputrade_app'@'localhost' IDENTIFIED BY 'ChangeMe_StrongPass!';
GRANT ALL PRIVILEGES ON cputrade.* TO 'cputrade_app'@'localhost';
FLUSH PRIVILEGES;
```

If you use a different password, update `spring.datasource.password` in
`src/main/resources/application.properties` (or export it as the
`SPRING_DATASOURCE_PASSWORD` environment variable instead of editing the file).

Tables are created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`).

## 3. Seed an admin account (once)

There's no self-registration path to ADMIN by design — insert the first one
directly. This row logs in as **username `admin`, password `Admin123!`**
(change the password before this ever runs anywhere but localhost):

```sql
INSERT INTO users (username, email, campus_handle, password_hash, role, verified, vendor_approved, created_at)
VALUES (
  'admin',
  'admin@cputmarket.ac.za',
  'admin@cputmarket.ac.za',
  '$2b$10$l5OcihujLu8354gkqn9v4ehF/J5vH2VG/nR9PazmMc.pRsq1w/R76',
  'ADMIN',
  true,
  true,
  NOW()
);
```

(Run this only after the app has started at least once so the `users` table
exists.)

## 4. Run it

```bash
./mvnw.cmd spring-boot:run
```

Starts on `http://localhost:8080`. CORS is pre-configured for the Vite dev
server at `http://localhost:5173` (override with `CORS_ALLOWED_ORIGIN`).

## 5. Quick smoke test (curl)

```bash
# Register a student
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jdoe","email":"jdoe@example.com","password":"Password1","role":"STUDENT"}'

# Log in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jdoe","password":"Password1"}'
# -> copy the "token" from the response

# Create a listing (replace $TOKEN)
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Calculus Textbook","price":0,"category":"TEXTBOOKS","condition":"GOOD"}'

# List active listings (public, no token needed)
curl http://localhost:8080/api/products
```

## Notes

- Email verification and vendor/removal notification emails are **stubbed**
  (logged only, not sent) — see `user/notification/NoOpEmailNotifier.java`.
  Swap in a real EmailJS/SMTP implementation of `EmailNotifier` later; nothing
  else needs to change.
- `spring.jpa.hibernate.ddl-auto=update` is meant for this fast single-dev
  build pass only — move to Flyway/Liquibase before a second developer or any
  real deployment touches this schema.
