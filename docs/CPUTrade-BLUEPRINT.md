# CPUTrade project blueprint

**Purpose:** Record the current application shape, implemented features, known gaps, and a reproducible local verification baseline for the school project. This describes the code as it exists; it does not prescribe hosting, deployment, or launch work.

## System overview

CPUTrade is a full-stack, modular-monolith campus marketplace:

```text
Mobile / desktop browser
  └─ Vue 3 SPA and installable PWA
       └─ Axios REST requests with Bearer JWT
            └─ Spring Boot API
                 ├─ Controllers → services → Spring Data JPA repositories
                 ├─ MySQL persistence
                 ├─ Local filesystem product images (/uploads/**)
                 ├─ Web Push using VAPID
                 └─ EmailNotifier interface (currently log-only)
```

The frontend and backend are separate codebases in this repository, but the backend is one Spring application. Business areas are separated into Java packages; they are not independently deployed services.

## Technology and code organization

| Area | Current implementation |
|---|---|
| Frontend | Vue 3, Vue Router, Pinia, Axios, Vite, and `vite-plugin-pwa`; source is under `frontend/src/`. |
| Frontend entry points | `frontend/index.html`, `frontend/src/main.js`, `frontend/src/App.vue`, and `frontend/src/router/index.js`. |
| Frontend screens | Marketplace, listing detail/form, login/register, checkout and order history, seller dashboard, wishlist, chats, bulletin board, notifications, admin, and appeals. |
| Backend | Java 21 project using Spring Boot 4.1.1, Spring MVC, Spring Security, Spring Data JPA/Hibernate, and Bean Validation. |
| Backend entry point | `backend/src/main/java/za/ac/cput/cputrade/CpuTradeBackendApplication.java`. |
| Backend packages | `admin`, `bulletin`, `chat`, `common`, `config`, `notification`, `order`, `product`, `profile`, `push`, `rating`, `security`, `storage`, `trust`, and `user`. |
| Persistence | MySQL through JPA; repositories and entities live beside their domain services. |
| External-style integrations | Product images use the configured local upload directory; email is abstracted behind `EmailNotifier`; push notifications use VAPID. |

Repository inventory at the Batch 0 baseline: 117 backend Java source files, 78 frontend `.js`/`.vue`/`.css` source files, 13 JPA entity classes, and 6 backend test classes. The current implementation adds the order domain and `PurchaseOrder` entity.

## User roles and core flows

- **Student:** registers with a CPUT email domain, browses and lists items, messages a seller, saves listings/searches, and can report or block accounts.
- **Vendor:** can browse after registration but requires administrator approval before selling.
- **Admin:** manages vendor approval, listings, reports, user suspension/reactivation, and appeals. The protected admin screen also shows the latest 100 recorded moderation and account actions.
- **Sale and rating:** seller marks a listing sold and may select an interested buyer. The selected buyer confirms receipt before rating; the backend prevents self-rating and duplicate ratings.
- **Order and checkout:** a signed-in buyer can order one listing at a time. Checkout records a local simulated payment, snapshots the listing title and price, and decrements stock transactionally. Sellers can mark an unreceived order ready for hand-off; buyers can cancel before receipt confirmation, restoring stock. The buyer confirms receipt from order history before rating; the seller can also view the order.
- **Community:** users browse the bulletin board, including Lost and Found posts.

## Data model at a glance

JPA currently defines 15 entities:

| Entity / table | Purpose and key relationships |
|---|---|
| `User` / `users` | Student, vendor, or admin identity; account status, verification flag, vendor approval, and activity timestamps. |
| `Product` / `products` | Listing owned by a seller; optional buyer on sale, stock quantity, campus, condition, sale/receipt state, and view count. Photos are an ordered `product_images` element-collection table. |
| `PurchaseOrder` / `marketplace_orders` | One-listing order linked to its product, buyer, and seller; stores title/price snapshots, quantity, simulated payment status, order status, and receipt timestamp. |
| `Conversation` / `conversations` | One buyer's conversation with a seller about a product. |
| `ChatMessage` / `chat_messages` | Messages in conversations, with sender and message lifecycle metadata. |
| `Rating` / `ratings` | A buyer's score/comment for a seller on a product; database uniqueness is enforced per product and rater. |
| `BulletinPost` / `bulletin_posts` | Community post, including Lost and Found content. |
| `WishlistItem` / `wishlist_items` | A user's saved product; user/product pair is unique. |
| `SavedSearch` / `saved_searches` | A user's saved marketplace search criteria. |
| `Notification` / `notifications` | In-app user notifications. |
| `PushSubscription` / `push_subscriptions` | Browser push endpoint and keys associated with a user. |
| `Report` / `reports` | User-submitted safety report and moderation state. |
| `UserBlock` / `user_blocks` | Blocker/blocked-user relationship; pair is unique. |
| `Appeal` / `appeals` | User appeal against an account suspension, with review status. |
| `AdminAction` / `admin_actions` | Append-only application history of moderation decisions with actor and target snapshots; no foreign keys to mutable users or listings. |

The entity annotations are the current schema source; Hibernate is configured to update the schema during application startup. No schema redesign is implied by this inventory.

## Feature status against the presenter guide

**Implemented in the application:** marketplace browse/search/filtering; multi-photo listings; free and swap listings; campus selection; listing details and sharing; role-based accounts and vendor approval; per-listing chat; one-listing simulated checkout and order history; seller hand-off status, buyer cancellation and buyer-confirmed receipt; sale ratings; wishlist and saved searches; in-app and Web Push notifications; seller dashboard; reporting, blocking, moderation, suspensions, appeals, and protected admin action history; bulletin board; PWA/offline support.

**Present, but limited or deliberately incomplete:**

| Feature | Current state |
|---|---|
| Student email verification | Student registrations require a CPUT email and remain unverified until a hashed, expiring, single-use token is confirmed. Local development logs the link; the optional SMTP profile sends actual mail. |
| Login throttling | Implemented in memory, per backend instance. Five failed attempts for a key are limited for 15 minutes; it is not shared across multiple processes. |
| Fraud prevention | Rule-based controls exist (domain check, vendor approval, reports, moderation, suspensions, rate limiting, and sale-linked ratings). There is no AI fraud detection. |
| Account lifecycle | Users can delete their account from settings after confirming their current password. The account is anonymized, private data and active listings are removed, and retained sale/moderation history displays a deleted-account label. |
| Orders and payment | Checkout creates one-item orders and records `SIMULATED`; the payment is not collected. Buyers may cancel before receipt confirmation and recover the reserved stock; sellers may mark an order ready for hand-off. There is no card-data collection, payment-provider integration, or escrow. The focused order integration test uses H2, and Playwright browser tests run against an isolated seeded H2 profile. |
| Admin action history | Admin decisions are recorded in an application-level append-only table and the admin UI shows at most 100 recent entries. Actor and target snapshots avoid foreign keys, but direct database access can still alter the records; this is not a tamper-evident audit system. |
| Testing | The backend has real-HTTP integration journeys for listing, chat, completed sale/rating, moderation, and checkout, in addition to unit, application-context, security, email-verification, and account-lifecycle tests. Frontend unit tests cover auth-store state, route guards, verification, account settings, checkout, and order history. Five Playwright browser flows cover checkout, seller restrictions, cancellation and stock restoration, hand-off, receipt confirmation, and rating. |
| Image storage | Validated JPEG/PNG images are stored on the local filesystem; this is not an object-storage integration. |

**Not implemented (potential future school-project scope):**

- A multi-item cart, real card payments, payment-provider integration, and escrow.
- Two-factor authentication.
- AI/ML fraud detection.
- A formal privacy policy and legal review of the account-data retention policy.
- A formal independent penetration test.
- Redis/shared distributed state, autoscaling, and microservices. The current app is a modular monolith; these are scale/architecture choices, not prerequisites for demonstrating the current local system.

## Security controls evidenced in source

- Passwords are hashed using BCrypt.
- API access uses expiring Bearer JWTs; the configured default lifetime is 24 hours.
- Backend authorization is enforced with Spring Security and service-level ownership/business checks; frontend route guards are only a user-experience convenience.
- Vendor approval, account suspension checks, and login throttling are enforced on the backend.
- Student email verification uses random single-use tokens; only SHA-256 hashes are stored, tokens expire, resends are throttled per account, and login is blocked until verification.
- Product image validation checks JPEG/PNG content signatures and size rather than trusting filenames.
- Development secret defaults exist for local use. Production-mode startup validation rejects known development defaults, but this blueprint does not constitute a security audit.

## Batch 0 verification snapshot

Run on 2026-10-15 using Java 25.0.4.1 on Windows. The project declares Java 21; repeat verification with Java 21 before treating that runtime as certified.

| Check | Result | Scope |
|---|---|---|
| Backend tests | **PASS** — 25 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0. | Run with `.\mvnw.cmd test` from `backend\`. A disposable MySQL 8.4 Docker container supplied the database for the Spring context-load test; the container was stopped after the run. The other 24 tests exercise authentication, JWTs, login limiting, image validation, and production-secret checks. |
| Frontend build | **PASS** — `npm run build`, exit code 0. | Run from `frontend\`; builds the Vue app and PWA service worker. |
| Frontend unit tests | **NOT CONFIGURED at Batch 0** | Vitest + Vue Test Utils + jsdom are added in Batch 1; see the Batch 1 results below. |

The backend suite is a useful starting baseline, not coverage of every user journey. The Batch 1 test additions cover key authentication and authorization boundaries, but do not replace end-to-end marketplace testing.

## Batch 1: security foundation and frontend unit tests

- Frontend unit tests use Vitest, Vue Test Utils, and jsdom; run with `npm test` from `frontend\\`.
- Auth-store tests cover persisted session restore, login success/failure, registration without automatic login, and logout cleanup.
- Router tests cover protected-route login redirects and admin/student role redirects. These guards remain a UX convenience; backend authorization is tested separately.
- JWT parsing rejects expected malformed/expired token errors without swallowing unexpected runtime or database failures from user lookup. Token validity is checked before querying user details.
- Backend security tests cover unauthenticated and malformed-token rejection, student/admin access to admin routes, suspension enforcement, pending-vendor restrictions, and unexpected user-lookup failure propagation.

| Check | Result | Scope |
|---|---|---|
| Backend tests | **PASS** — 33 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0. | Full backend suite against a disposable MySQL 8.4 Docker container. Java 25.0.4.1 was used; the project declares Java 21. |
| Frontend unit tests | **PASS** — 8 tests, exit code 0. | Run with `npm test` from `frontend\\`; 5 auth-store tests and 3 route-guard tests. |
| Frontend build | **PASS** — `npm run build`, exit code 0. | Vue app and PWA service worker build. |

## Recommended follow-on batches

1. **Browser end-to-end testing:** exercise the marketplace through the Vue UI; current journey coverage is at the backend HTTP/API layer.
2. **Privacy policy and retention review:** document the prototype's retention choice and review it before any real-world use.
3. **Two-factor authentication:** add step-up authentication and a tested recovery flow if required by the course brief.
4. **Real payment integration:** only if the course brief requires it, evaluate a payment provider and its security/compliance requirements as a separate project. The current simulated checkout is not a payment integration; keep escrow and real-money service out of scope until separately reviewed.

For every batch: define acceptance criteria, make the smallest complete change, add focused tests, run backend tests and the frontend build as relevant, and record remaining gaps. Hosting, deployment, and launch are explicitly outside this plan.

## Batch 2: student email verification

- Student registration requires a CPUT email domain and starts with `verified=false`; vendors remain email-domain agnostic and use admin approval.
- Verification tokens are cryptographically random, single-use, expire after a configurable period, and are stored only as SHA-256 hashes.
- A database-atomic token consumption operation prevents replay and rejects expired tokens. Resend requests return the same success status for known and unknown email addresses, with a per-account cooldown.
- Students cannot sign in until their email is verified.
- The local profile prints the verification link to the backend log. The explicit `smtp` profile uses configured SMTP credentials to send mail; the verification link points to the configurable frontend base URL.
- Verification is user-confirmed in the frontend rather than automatically consumed by a GET request, avoiding common email link-scanner activation.

### Batch 2 verification snapshot

Run on 2026-10-15 on Windows. Backend tests used Java 25.0.4.1 (the project declares Java 21) and disposable MySQL 8.4.11 in Docker.

| Check | Result | Scope |
|---|---|---|
| Backend tests | **PASS** — 43 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0. | Full `.\mvnw.cmd test` suite against MySQL 8.4. Includes registration/login gating, verification, expiry, replay, and resend integration coverage. The temporary database container was removed after the run. |
| Frontend unit tests | **PASS** — 13 tests, exit code 0. | `npm test` from `frontend\`; auth state, route guards, resend UI, and verification view. |
| Frontend build | **PASS** — `npm run build`, exit code 0. | Vue app and PWA service worker. |
| SMTP delivery | **NOT VERIFIED** | No SMTP account was configured. The default local logging adapter was exercised during integration tests. |
| Browser end-to-end suite | **NOT CONFIGURED** | Vue component/unit tests and backend integration tests pass; a complete browser-based marketplace flow remains future work. |

## Batch 3: account privacy and lifecycle

- `DELETE /api/account` requires an authenticated session and the current password. The settings page also requires the user to type `DELETE` before submitting.
- The account receives replacement identifiers and a random password hash, its status becomes `DELETED`, and login is blocked. Public profiles and presence no longer expose the original identity.
- Active listings are deactivated and their descriptions, photos, and swap details are removed. Private conversations and messages, authored bulletin posts, wishlists, saved searches, notifications, push subscriptions, and blocks are removed; comments written on ratings are redacted.
- Sale records, rating scores, reports, and appeals remain linked to the anonymized user ID and display the account as deleted. This indefinite retention choice is for the school-project prototype only; it has not had legal review and is not a claim of POPIA compliance.

### Batch 3 verification snapshot

Run on 2026-10-15 on Windows. Backend tests used Java 25.0.4.1 (the project declares Java 21) and disposable MySQL 8.4 in Docker.

| Check | Result | Scope |
|---|---|---|
| Backend tests | **PASS** — 46 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0. | Full backend suite against disposable MySQL 8.4. |
| Frontend unit tests | **PASS** — 16 tests, exit code 0. | `npm test` from `frontend\`; includes account-settings confirmation and success/failure handling. |
| Frontend build | **PASS** — `npm run build`, exit code 0. | Vue app and PWA service worker. |
| Local lifecycle API probe | **PARTIAL PASS** — registration 201, login 200, wrong-password deletion 403, correct-password deletion 204. | Verified against the local backend and disposable MySQL runtime. Rejection of the pre-deletion bearer token was not confirmed by the manual probe. |
| Java 21 runtime | **NOT VERIFIED** | The test and local runtime used Java 25.0.4.1. |

The automated suite includes account-anonymization and deleted-account login tests. The indefinite history-retention choice remains a prototype assumption and must be reviewed before real-world use.

## Batch 4: marketplace API journey tests

- Added random-port, real-HTTP integration tests for listing creation/search/ownership, buyer-seller messaging, seller-marked sales with buyer receipt confirmation and rating eligibility, and report moderation.
- Assertions cover authorization boundaries and persisted database state. These tests create unique fixtures, so run the backend suite against a disposable local MySQL database.
- Updated the admin authorization test to validate the response shape without assuming the users table is empty.

### Batch 4 verification snapshot

Run on 2026-10-15 on Windows. Backend tests used Java 25.0.4.1 (the project declares Java 21) and disposable MySQL 8.4.11 in Docker.

| Check | Result | Scope |
|---|---|---|
| Backend tests | **PASS** — 50 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0. | Full `.\mvnw.cmd test` suite; includes all four new real-HTTP marketplace flows. |
| Frontend unit tests | **PASS** — 16 tests, exit code 0. | `npm test` from `frontend\`. |
| Frontend build | **PASS** — `npm run build`, exit code 0. | Vue app and PWA service worker. |
| Browser end-to-end suite | **NOT CONFIGURED** | API-level HTTP flows are covered; browser interaction flows remain future work. |

## Current verification snapshot

Run on 2026-10-16 on Windows. Browser validation used Java 21 and an isolated
seeded H2 database; it did not use the developer's MySQL database.

| Check | Result | Scope |
|---|---|---|
| Full backend tests | **PASS** — 55 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0. | Full `.\mvnw.cmd test` suite on Java 21. The verification-resend row lock uses MariaDB-compatible `SELECT ... FOR UPDATE` SQL. |
| Focused order integration tests | **PASS** — 4 tests, exit code 0. | `OrderFlowIntegrationTest` against H2. |
| Focused moderation/audit integration tests | **PASS** — 2 tests, exit code 0. | `ModerationFlowIntegrationTest`, including action snapshots and admin-only history. |
| Frontend unit tests | **PASS** — 27 tests, exit code 0. | Vitest suite. |
| Frontend build | **PASS** — `npm run build`, exit code 0. | Vue app and PWA service worker. |
| Browser end-to-end suite | **PASS** — 5 tests, exit code 0. | Checkout, seller restriction, cancellation with stock restoration, seller hand-off followed by buyer receipt, and receipt/rating; seeded accounts and isolated H2. |
