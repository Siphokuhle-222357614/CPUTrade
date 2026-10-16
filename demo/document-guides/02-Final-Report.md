# 02 · Final Report: writing guide

**Owner suggestion:** Member 1. **Length:** 6 to 10 pages. **Time:** 45 to 60 minutes if you reuse your Term 1 and 2 work.

## Purpose

The brief calls it "a concise document that provides a brief background, problem statement, project objectives, scope and limitations, systems analysis (requirements) and testing and evaluation". It is the main document markers read. It tells the story: *what problem, what you promised, what you built, how you know it works, what is missing.*

## What markers are likely to look for *(my inference from the brief)*

- A clear problem and measurable objectives, and whether the final system meets them.
- Honest scope and limitations (they know the brief asked for payments and microservices).
- Requirements that trace to features, and testing that traces to requirements.
- Evidence: diagrams, screenshots, test results.
- Project management thinking, not only technical description.

## Structure (follow this order) and how to write each part

### Cover page and contents
Title, course code PRM370/371/372S, group name and members, date. Add a short contents list.

### 1. Background (half a page)
**What it is:** the situation that made the project necessary.
**Write about:** how students and the surrounding community currently buy and sell second-hand goods; why informal groups (WhatsApp, Facebook) fall short (no verification, scams, no records, not campus-specific); why a campus-and-community marketplace helps (affordability, sustainability, community).
**Use:** the brief's introduction, your Term 1 charter, system facts in 01 section 1.

### 2. Problem statement (one paragraph)
**What it is:** one precise paragraph. Test it: who suffers, how, and what is the consequence?
**Structure to follow:** the people affected, the problem, the impact, what is needed.

### 3. Project objectives (4 to 6 bullets)
**What it is:** what you set out to achieve, written so you can say yes or no at the end. Make them SMART (specific, measurable, achievable, relevant, time-bound).
**Consider covering:** verified identity for students; trustworthy ratings; easy listing and search; moderation and safety; mobile access (installable); community bulletin board; delivery on the project schedule.
**Tip:** for each objective note later, in section 6, whether it was met and how you know.

### 4. Scope and limitations (1 to 1.5 pages)
- **In scope:** the features list from 01 section 3 (summarise in a table by module).
- **Out of scope / limitations:** be direct. Use the table in 01 section 8. For each: what the brief asked, what you did instead, why.
- **Constraints:** team of three, fixed timeline, student budget (free tools, no hosting or payment accounts), local deployment.
- **Assumptions:** students own their CPUT mailbox; users meet on campus and pay in person.
**Why this matters:** a well-argued limitation protects your marks.

### 5. Systems analysis (2 to 3 pages)

**5.1 Stakeholders and users** (3 to 4 lines, refer to the stakeholder matrix).

**5.2 Requirements.** Explain functional requirements (what the system does) and non-functional requirements (how well: security, usability, performance, compatibility). Reuse your Term 2 backlog and user story IDs. Table to fill:

| ID | Requirement | Type | Priority (MoSCoW) | Status (Done / Partly / Not done) | Evidence |
|---|---|---|---|---|---|
| FR-01 | *Example:* Students register with a CPUT email | Functional | Must | Done | Test T1, T2 |
| FR-02 | | | | | |

**Requirement ideas drawn from the real system** (adapt, merge, and match them to your backlog): CPUT-only student registration; vendor approval; login with expiring session; create/edit/delete listings with photos; search and filters; swap and Freecycle; chat per listing; mark sold to a named buyer; buyer confirms receipt; rating after confirmed receipt; wishlist with alerts; saved searches; in-app and push notifications; seller dashboard; report and block; admin moderation, suspension and appeals; bulletin board with Lost and Found; installable mobile app; passwords hashed; login rate limiting; images validated.
**Partly done from the brief:** one-listing simulated checkout and order history are implemented; there is no multi-item cart, real payment integration or escrow. **Not done:** AI fraud detection, 2FA, microservices and caching.

**5.3 Design.** Include two diagrams:
1. **Architecture diagram:** browser (Vue PWA) to REST API (Spring Boot) to database; mention the security filters and photo storage. Draw it from 01 section 4.
2. **Data model (ER diagram):** the 13 tables and relationships in 01 section 5.
Optional: a sequence diagram of the verified sale (seller marks sold, buyer confirms, buyer rates) and 2 or 3 screenshots from `demo/screenshots/`.

**5.4 Technology choices.** One short table: technology, why chosen (for example: Vue for fast mobile-first UI and PWA; Spring Boot for secure, structured APIs; MySQL for relational data like users-listings-ratings).

### 6. Testing and evaluation (2 pages)

**6.1 Approach.** Describe your testing: automated unit tests, manual scenario testing, live checks. Say who tested.

**6.2 Results.** Facts you can report:
- All 55 backend tests, 27 frontend Vitest tests, and 8 Playwright browser flows passed. The backend suite includes 4 order lifecycle tests and 2 moderation/audit tests. Coverage includes CPUT-email validation (including lookalike domains), email verification and login gating, vendor approval, password hashing, account anonymization, token validity/expiry/forgery, image validation, login rate limiting, marketplace filters, buyer/seller chat persistence, admin report review/action history, checkout/order behavior, auth UI and route guards.
- Live checks: brute-force login blocked on the 6th attempt; oversized and fake image uploads rejected; a 3.8 MB photo accepted.
- **A defect found and fixed:** six simultaneous "mark sold" requests reduced stock by one instead of six (a race condition). Fixed with a database lock, retested (50 to 44).
- Browser E2E covers simulated checkout, order history, marketplace search/filters, buyer/seller chat persistence, and admin report review/action history. Vendor approval, suspension/appeals, bulletin, wishlist and PWA installation remain manual or backend-covered rather than browser-automated.

**6.3 Test cases.** Run these yourselves and fill in "Actual" and "Pass/Fail" honestly:

| ID | Scenario | Steps | Expected | Actual | Pass? |
|---|---|---|---|---|---|
| T1 | Student must use CPUT email | Register STUDENT with a gmail address | Rejected with message | | |
| T2 | Student registers with CPUT email | Register with x@mycput.ac.za | Account created unverified; sign-in blocked until link confirmation | | |
| T3 | Vendor needs approval | Log in as quickfix_it, try to create a listing | Blocked until admin approves | | |
| T4 | Wrong-password lockout | 6 wrong logins for one username | 6th returns "Too many attempts" | | |
| T5 | Create listing with photo | Dashboard, New Listing, add a 3 MB JPEG | Listing shows photo | | |
| T6 | Oversized photo | Add a 5 MB JPEG | Clear error, not saved | | |
| T7 | Chat per listing | Buyer messages seller, seller replies | Both see messages | | |
| T8 | Sale to a named buyer | Seller marks sold to buyer | Listing shows Sold, buyer notified | | |
| T9 | Rating needs confirmed receipt | Buyer tries to rate before confirming | Blocked | | |
| T10 | Rating after confirmation | Buyer confirms then rates 5 stars | Rating saved, seller average updates | | |
| T11 | Cannot rate twice | Rate the same sale again | Rejected | | |
| T12 | Report and moderate | Report a listing; admin reviews | Appears in queue, admin can act | | |
| T13 | Suspension works at once | Admin suspends a logged-in user | User blocked on next request | | |
| T14 | Wishlist alerts | Heart an item; seller drops price | Notification arrives | | |
| T15 | Install as PWA | Chrome install prompt | Installs and opens | | |
| T16 | Simulated single-listing checkout | Buyer checks out one available listing | Order appears in history, stock decreases, and payment status is `SIMULATED`; no charge or card data | | |

**6.4 Evaluation against objectives.** A table: objective, met (yes / partly / no), evidence. Then 2 or 3 paragraphs: what worked, what did not, what you would improve.

### 7. Conclusion and recommendations (half a page)
Summarise what was delivered, how it answers the problem, the key limitations, and next steps (broader browser tests for marketplace browsing, chat and admin; a hosted payment gateway and 2FA only if real payments become a requirement; cloud photo storage if needed beyond local use; privacy policy and account-deletion review; independent penetration test).

### References
List anything you quote or use (the course textbook, Spring Boot and Vue documentation, photo credits). Use the referencing style your department requires.

## Common mistakes to avoid

- Claiming features the system does not have (payments, escrow, AI, microservices).
- Describing code instead of explaining the *project* (this is a project management course).
- Objectives that cannot be measured.
- Test results you did not actually run.
- Copying text from the brief, the README or another document without rewriting.

## Checklist

- [ ] Background, problem, objectives, scope, limitations, analysis, testing, conclusion all present
- [ ] Two diagrams (architecture, data model) and 2 to 3 screenshots
- [ ] Requirements table with status and evidence
- [ ] Test table filled with real results
- [ ] Limitations stated honestly with reasons
- [ ] Names, features and numbers match the other documents
