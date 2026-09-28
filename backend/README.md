# CPUTrade Backend

Spring Boot 4.1.1 (Java 21) REST API for CPUTrade: JWT auth, listings, chat,
ratings, notifications, Web Push, and admin moderation.

**Setup and run instructions are in the [main README](../README.md):**

- [Create the database](../README.md#7-step-1--create-the-database): `db/setup.sql`
- [Start the backend](../README.md#8-step-2--start-the-backend): IntelliJ, VS Code or `.\mvnw.cmd spring-boot:run`
- [Add the admin and demo data](../README.md#9-step-3--add-the-admin-account-and-optional-demo-data): `db/seed-admin.sql`, `db/seed-sample-data.sql`
- [Configuration / env vars](../README.md#12-configuration)
- [Useful database queries](../README.md#13-useful-database-queries)
- [API reference](../README.md#14-api-reference)

## `db/` scripts

| Script | When to run it |
|---|---|
| `setup.sql` | Once, as MySQL `root`. Creates the `cputrade` database and the `cputrade_app` user. |
| `seed-admin.sql` | After the backend's first start. Creates `admin` / `Admin123!`. |
| `seed-sample-data.sql` | Optional, after the first start. Adds demo users (password `Password123!`), listings and bulletin posts. |

Tables are created by Hibernate on startup (`spring.jpa.hibernate.ddl-auto=update`).
Don't create them by hand.
