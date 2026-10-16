# CPUTrade: likely questions, honest answers and security reasons

**Purpose:** so you never lose marks for something you did not build, and never claim something you did not build.
Every statement below is true of the current system (checked against the code on 16 Oct 2026).

## The one rule

**Never claim a feature that does not exist.** An honest gap with a good reason scores better than a false claim that an
examiner disproves in one click. Use this four-step answer every time:

1. **Acknowledge** it plainly: "We did not build X."
2. **Reason**: why (scope, time, risk, security).
3. **What we did instead**: the control that covers the same need.
4. **Next step**: how we would add it.

Be ready to explain any screen or feature you show, and be truthful about how the system was built if you are asked.

---

## 1. The brief versus what we built

| Brief item | Status | What we have | Honest reason |
|---|---|---|---|
| Multi-role accounts | Built | Student, Vendor, Admin with different permissions | |
| Product listings | Built | Up to 6 photos, quantity, condition, campus, swap, Freecycle | |
| Search and filters | Built | Text, category, campus, price, "Open to Trade" | |
| Reviews and ratings | Built | One rating per sale, only after the buyer confirms receipt | |
| Notifications | Built | In-app centre plus Web Push to the device | |
| Community bulletin board | Built | General, Lost and Found | |
| Student authentication via university email | Built | CPUT email domain is checked and students must confirm a single-use, expiring link before login | Local profile logs the link; SMTP delivery requires configuration |
| Vendor verification | **Partly** | Vendors cannot do anything but browse until an admin approves them | Manual check; no automatic business-registration lookup |
| Moderation and flagging | Built | Reports, blocking, listing removal, suspensions, appeals | |
| Admin action history | Built | Admin-only recent history of moderation decisions with actor and target snapshots | Application-level append-only only; direct database access can alter entries |
| Mobile app and/or web portal | Built | Installable web app (PWA), mobile-first | |
| Cart and checkout | **Partly built** | One-listing simulated checkout, order history, seller hand-off readiness, and buyer cancellation before receipt | No real charges or card data; see question 1 |
| Payment integration (PayFast, SnapScan) | **Not built** | Checkout records a `SIMULATED` payment status | No provider or real-money processing; see question 1 |
| Escrow for expensive items | **Not built (partial control)** | Buyer-confirmed receipt gates ratings | No money is held, so it is not escrow |
| Two-factor authentication | **Not built** | Rate limiting, bcrypt, token expiry | See question 4 |
| AI-driven fraud detection | **Not built** | Rule-based controls, reports, moderation | See question 3 |
| Microservices architecture | **Not built** | Modular monolith (11 feature modules) | See question 5 |
| Cloud hosting, auto-scaling, load balancing | **Not built** | Runs locally; deployment guide written | See question 6 |
| Redis caching | **Not built** | Database queries, browser offline cache | See question 6 |
| HTTPS and encryption | **Partly** | Passwords hashed; HTTPS is set up at deployment | Local demo runs on plain HTTP |
| Regular penetration testing | **Not done** | Our own security checks and automated backend, frontend and browser tests | See question 9 |

---

## 2. Likely questions and solid answers

### Payments and money

**1. "Why is there no multi-item cart or payment gateway?"**
- *Reason (scope):* the group is three people on a fixed timeline; a real gateway also needs a merchant account and live credentials we do not have.
- *Reason (security):* by not taking payments, the system **never receives, stores or processes card data**. That removes the biggest breach risk and the compliance burden that comes with card data (PCI DSS) and with financial regulation.
- *What we built:* a local, one-listing simulated checkout that creates an order, updates stock and records `SIMULATED` payment status. It does not charge anyone or collect card data. For a real trade, the buyer and seller can still agree in chat, meet on campus using the safe meet-up suggestions, and pay in person; the **buyer confirms receipt** before any rating is allowed.
- *Next step:* only if real payments become a project requirement, integrate a provider's hosted checkout so card details go straight to the gateway and never touch our server.

**2. "Is the buyer confirmation an escrow?"**
- No, and we do not call it one. Escrow holds money; our simulated checkout holds no money. Receipt confirmation is a **trust control**: a rating cannot exist unless the named buyer confirmed they received the item, so sellers cannot write their own reviews and fake reviews are blocked.
- *Next step:* escrow would require a separate product, legal and provider review; it is not part of the current project.

### Fraud and accounts

**3. "The brief asks for AI fraud detection. Where is it?"**
- We did not build machine-learning detection. With no real transaction history there is no meaningful data to train on, and a poorly trained model would raise false alarms against real students.
- *What we did instead (all real and demonstrable):* CPUT-only student email rule; vendor approval; reporting by users; admin queue; blocking; suspensions that take effect on the very next request, with appeals; **login rate limiting**; rating rules that stop self-rating, double rating and rating before confirmed receipt.
- Consequential vendor, listing, report, account, and appeal decisions also appear in a role-protected admin history with an actor and target snapshot. It improves accountability but is not a tamper-evident or externally backed audit log.
- *Next step:* rule-based alerts first (for example many reports against one account, or a brand-new account posting many listings), then models once there is data.

**4. "Why no two-factor authentication?"**
- No sensitive money moves through the system, so the highest-risk action 2FA protects (high-value payments) does not exist yet. The brief ties 2FA to high-value purchases.
- *What protects accounts now:* bcrypt password hashing, minimum 8-character passwords, 24-hour token expiry, student email verification, and rate limiting (5 wrong passwords locks that username from that address for 15 minutes, HTTP 429).
- *Honest limit:* password rules do not enforce complexity. *Next step:* 2FA (one-time code or authenticator app) if payments or other high-risk actions are introduced.

**5. "How do you know a student really owns that CPUT email?"**
- Registration checks the **domain** (and rejects lookalike domains such as `mycput.ac.za.evil.com`) and then requires confirmation through a random, expiring, single-use link before login. The token is stored only as a hash, and resends are throttled and non-enumerating.
- In local mode the link is written to the backend log so the flow can be completed without an email provider; SMTP delivery is optional configuration.

### Architecture and scale

**6. "The brief says microservices, cloud, auto-scaling and Redis. Is this scalable?"**
- It is a **modular monolith**: one deployable application split into clear modules (user, product, chat, trust, rating, notification, bulletin, admin and others). For a team of three and a campus-sized user base, this is faster to build and test and has fewer failure points than a dozen services. Modules have clear boundaries, so they can be split into services later.
- *Scalability facts:* the API is stateless (JWT, no server sessions), so several copies can run behind a load balancer. We are honest about two things that must move to shared services first: the login rate limiter (in memory) and photo storage (local disk, to be object storage).
- *Caching:* no Redis. We rely on the database and a browser-side offline cache for the feed. Redis is a measured next step if load requires it.
- *Hosting:* we ran it locally; DEPLOYMENT.md documents the environment variables, HTTPS and production settings for a real host.

**7. "What happens if two people buy at the same time?"**
- We found a real race during testing: six simultaneous "mark sold" requests reduced stock by only one. We fixed it with a database row lock and re-tested (stock went from 50 to 44 as expected). Duplicate-submission errors now return a clean 409 message.

### Data and security

**8. "What personal data do you hold and how is it protected?"**
- For active accounts, stored data includes username, email, **hashed** password, chat messages, listings and photos, ratings, and (optionally) a push-notification subscription. **No card data, no ID numbers.**
- Protections: passwords never stored in plain text (bcrypt); role-based access (admin pages are admin-only; admins cannot be self-registered); suspended and unapproved accounts are blocked on every request, not just at login; a user can only read their own conversations and edit their own messages (every chat endpoint checks the user is a participant); secrets are read from environment variables, and a production-mode startup check **refuses to start** if the dev secrets are still in use.
- Account deletion is self-service and password-confirmed. It anonymizes the account and removes private account data and active listings, while sale records, rating scores, reports and appeals remain linked to the anonymized user ID and appear under "Deleted account."
- *Honest limits (say these before being asked):* there is no data export or formal privacy policy. Anonymized sale and moderation history is retained indefinitely for this school-project prototype; that choice has not had legal review and is not a claim of POPIA compliance.

**9. "Did you do penetration testing?"**
- No professional or third-party penetration test. What we did do: automated tests for the security rules (email domain, email verification, vendor approval, password hashing, account anonymization, token validity, expiry and forgery, image validation, rate limiting, and production secrets check); all 55 backend tests, 27 frontend unit tests, and 8 browser E2E flows pass. The order integration tests (4) and moderation/audit tests (2) pass within the backend suite. Browser flows now cover marketplace filtering, buyer/seller chat persistence, admin report review and action history, and checkout/order behavior. Live attack-style checks include brute-force login (locked out on the 6th attempt), oversized and fake image uploads (rejected), and the concurrency test above.
- *Next step:* an independent penetration test before any public launch.

**10. "How do you protect against common web attacks?"**
- *SQL injection:* all queries go through the JPA layer with bound parameters. We use no string-built SQL.
- *Cross-site scripting (XSS):* the Vue front end escapes everything it displays, and the code never uses `v-html`.
- *CSRF:* the API uses Bearer tokens in a header, not cookies, so browsers do not attach credentials automatically; CORS allows only the configured front-end origin.
- *Malicious uploads:* we check the file's actual header bytes (JPEG or PNG only, not the file name), enforce a 4 MB limit, and store files under random names.
- *Brute force:* login rate limiting.
- *Known trade-off (be upfront):* the login token is kept in the browser's local storage, which is simple and works for a PWA but is exposed if an XSS flaw ever existed. That is why we escape all output; httpOnly cookies are a future hardening step.

**11. "Dev secrets are in the code. Isn't that a risk?"**
- They exist only so the project runs on a laptop. In production mode (`SPRING_PROFILES_ACTIVE=prod`) the app **refuses to start** if the JWT secret, database password or push key still has the dev value, or if the JWT secret is shorter than 32 characters. This is covered by automated tests. DEPLOYMENT.md lists what to set.

### Process and quality

**12. "How did you test?"**
- All 55 backend tests, 27 frontend Vitest tests, and 8 Playwright browser flows passed. The order integration tests cover 4 lifecycle cases, and 2 moderation/audit integration tests cover action snapshots and admin-only history. Browser tests cover marketplace search/filters, cross-role chat and persisted messages, report review and admin action history, checkout restrictions, cancellation and stock restoration, hand-off, receipt confirmation, and rating. The CI workflow is in the repository, but we have not yet seen it run on GitHub. A real concurrency defect was found and fixed (question 7).
- *Honest limit:* vendor approval, account suspension and appeals, bulletin-board flows, wishlist alerts, and PWA installation still need broader browser automation or manual verification; backend integration tests cover some related API behavior.

**13. "Why is the database set up automatically instead of migrations?"**
- Hibernate creates the tables for fast iteration in a student project. Documented next step: Flyway migrations before a second environment or developer, so schema changes are reviewable and repeatable.

**14. "What would you do next?"**
- In order: formal privacy policy and legal review of the retention choice; broaden browser automation for vendor approval, suspension/appeals, bulletin, wishlist, and PWA installation; payment integration and 2FA only if required; cloud photo storage and shared rate limiting if the system later needs multi-instance operation; independent penetration test before real-world use.

---

## 3. Security controls at a glance

| Threat | Control | Evidence |
|---|---|---|
| Stolen passwords from the database | bcrypt hashing | Automated test: stored value differs from the password and still verifies |
| Password guessing | Rate limit: 5 failures, 15-minute lock, HTTP 429 | Unit tests plus live test (attempt 6 blocked) |
| Fake student accounts | CPUT email domain check, verified-address login gate, expiring single-use tokens, throttled resend | Unit and integration tests |
| Fake businesses | Vendor approval; vendors blocked from writes until approved | Checked on every request |
| Forged or expired login tokens | Signed JWT, 24-hour expiry | Unit tests (forged, expired, wrong user) |
| Abuse by a logged-in user | Suspension effective on the next request; appeals | Filter runs on every request |
| Fake reviews | Buyer-confirmed receipt; one rating per sale; no self-rating | Rules in the rating service; database unique key |
| Double-submit and race bugs | Transactional stock updates on sale and checkout; 409 on duplicates | Live concurrency test (50 to 44); checkout integration tests |
| Malicious files | Header-byte check, JPEG/PNG only, 4 MB, random file names | Unit tests plus live upload test |
| Admin takeover | Admin role cannot be self-registered; admin endpoints admin-only | Registration validation; security config |
| Secrets in production | Startup check refuses dev secrets | Unit tests |
| Injection and XSS | Bound parameters; Vue output escaping; no `v-html` | Code review |

## 4. Say these limits first (before an examiner finds them)

- No multi-item cart, real payment processing or escrow. Checkout is one-listing and simulated; no money is held or card data collected.
- Student verification links are logged in local mode; actual email delivery needs the SMTP profile and configuration.
- No AI fraud detection and no two-factor authentication.
- Modular monolith, not microservices; no Redis; not deployed to the cloud.
- No data export, no formal privacy policy, no independent penetration test. Anonymized sale and moderation history is retained indefinitely for the prototype only; this has not had legal review and must not be presented as POPIA compliance.
- Rate limiter and photo storage are single-server for now.

## 5. Words to avoid and words to use

| Avoid | Say instead |
|---|---|
| "Fully secure" or "unhackable" | "We implemented these specific controls and tested them" |
| "AI-powered fraud detection" | "Rule-based fraud controls, with ML as a future step" |
| "Microservices" | "Modular monolith that can be split later" |
| "Escrow" | "Buyer-confirmed receipt" |
| "Encrypted payments" | "We do not process payments, so no card data is held" |
| "Verified students" | "Students must use a CPUT email address" |
| "Production ready" | "Ready for a campus pilot, with the listed next steps" |
