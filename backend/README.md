# CPUTrade Backend

Spring Boot 4.1.1 (Java 21) REST API — JWT auth, product listings, admin
moderation. Covers the must-have user stories only (see the root README).

## 1. Prerequisites

- JDK 21 (Temurin recommended)
- MySQL 8.x running locally
- No separate Maven install needed — use the bundled wrapper (`./mvnw.cmd` on
  Windows, `./mvnw` on macOS/Linux)

## 2. Database setup (once)

Run `db/setup.sql` against your local MySQL as an admin user (e.g. `root`) —
see the step-by-step guide below. It creates the `cputrade` schema and a
dedicated `cputrade_app` user. Tables themselves are created automatically the
first time the app boots (`spring.jpa.hibernate.ddl-auto=update`).

If you use a different password than the one in `db/setup.sql`, update
`spring.datasource.password` in `src/main/resources/application.properties`
(or export it as the `SPRING_DATASOURCE_PASSWORD` environment variable instead
of editing the file).

### Step-by-step (Windows, using the `mysql` CLI)

1. Open a terminal and connect as your MySQL admin user (usually `root`):
   ```
   mysql -u root -p
   ```
   Enter your MySQL root password when prompted.
2. Run the setup script from inside the `mysql>` prompt:
   ```
   source C:/Users/User1/Downloads/GiftedDev/GiftedDev Projects/CPUTrade/backend/db/setup.sql
   ```
   (Adjust the path if your checkout lives elsewhere. On Windows, forward
   slashes work fine here even though the rest of the path uses backslashes.)
3. Verify it worked:
   ```
   SHOW DATABASES LIKE 'cputrade';
   SELECT User, Host FROM mysql.user WHERE User = 'cputrade_app';
   ```
   Both should return one row.
4. Exit MySQL: `exit`
5. Confirm the new user can actually connect:
   ```
   mysql -u cputrade_app -p cputrade
   ```
   (password: `ChangeMe_StrongPass!`, unless you changed it in the script
   first). If you get `ERROR 1045 (Access denied)`, the script either didn't
   run or you're using a different password than what's in
   `application.properties` — fix one to match the other.
6. Start the backend once (`./mvnw.cmd spring-boot:run`, see §4 below) so
   Hibernate creates the `users` and `products` tables. Stop it once it's up
   and logging normally (Ctrl+C).
7. Seed an admin account (there's no self-registration path to ADMIN by
   design):
   ```
   mysql -u root -p cputrade < backend/db/seed-admin.sql
   ```
   or `source backend/db/seed-admin.sql` from inside a `mysql -u root -p`
   session. This logs in as **username `admin`, password `Admin123!`** —
   change the password before this ever runs anywhere but localhost.
8. Start the backend again — it's now ready to use.

Prefer a GUI? MySQL Workbench works the same way: open a connection as root,
open `db/setup.sql` as a SQL script and execute it, then (after the app's
first boot) do the same with `db/seed-admin.sql`.

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
