# CPUTrade portfolio kit (PRM370/371/372S, Term 3 submission)

This is a **kit, not the documents**: outlines, blank templates, prompts, and verified facts about the system.
Every answer, rating, date and reflection has to come from your group. Where I give an example it is marked *(idea)*.

---

## 0. The 45-minute plan for 3 people

| Min | Do |
|---|---|
| 0-5 | Everyone reads sections 1 and 2 of this file. Assign owners (below). Open your **Term 1 and Term 2 submissions**: the charter, stakeholder analysis, risk register, communication plan, retrospectives and change records already hold most of what you need. **Update them, don't restart.** |
| 5-30 | Each owner writes their documents from the outlines and templates below, in their own words. |
| 30-40 | Swap documents and cross-check: facts match section 1, names/dates/roles consistent everywhere, the RACI matches the team list. |
| 40-45 | Compile into ONE portfolio file (PDF/Word), add contribution page and signed plagiarism declaration as the last page. Export. |

| Owner | Documents |
|---|---|
| Member 1 (PM) | Final Report (all sections, including testing and evaluation) + compile the portfolio |
| Member 2 | Stakeholder Engagement Matrix + Resource Plan (RACI and allocation) + Risk Management Plan (Risk Register and RAID log) |
| Member 3 | Quality Management Plan + Transition and Closure Plan + Reflection and Lessons Learned + contribution page |

With three people the load is heavier, so reuse your Term 1 and Term 2 documents heavily and keep each document short but complete.
Everyone also helps with the video.

---

## 1. Fact sheet about the system (verified from the repo, 9 Oct 2026)

Use these so your documents match the real product. Cite them in your own sentences.

**What it is:** CPUTrade, a mobile-first campus marketplace for CPUT students and approved outside vendors: buy, sell, swap, and a community bulletin board. Installable PWA.

**Stack:** Backend Spring Boot 4 (Java 21), Spring Security + JWT, Spring Data JPA/Hibernate. Database MySQL/MariaDB. Frontend Vue 3, Vite, Vue Router, Pinia, Axios, PWA with Web Push.

**Size:** 39 git commits since 14 Sep 2026; about 65 API endpoints; 10 feature modules (admin, bulletin, chat, notification, product, profile, push, rating, trust, user) plus shared common/config/security/storage; 13 database tables (users, products, product_images, conversations, chat_messages, ratings, wishlist_items, saved_searches, notifications, push_subscriptions, bulletin_posts, reports, appeals, user_blocks); 12 screens.

**Roles:** STUDENT (CPUT email only: `@mycput.ac.za` / `@cput.ac.za`), VENDOR (any email, **needs admin approval**), ADMIN (cannot be self-registered).

**Features:** listings with up to 6 photos (JPEG/PNG, now max **4 MB** each); search, category, campus (5 campuses) and price filters; swap/barter; Freecycle (R0); mark as sold to a chosen buyer; **buyer confirms receipt before rating** the seller; chat with read receipts and safe meet-up suggestions; wishlist with price-drop and sold alerts; saved searches; in-app + Web Push notifications; seller dashboard; reports, blocking, admin moderation, suspensions and appeals; bulletin board with Lost and Found; WhatsApp share; offline cache.

**Security measures:** bcrypt password hashing; JWT (24h expiry); role-based access; CPUT-only email rule; suspension takes effect immediately; image header check (not just file name); **login rate limiting** (5 wrong passwords per username+address, 15-minute lock, HTTP 429); **startup check** that refuses dev secrets in production mode; database row lock on sale actions.

**Testing done (these are facts you can report):**
- 25 automated backend tests, all passing: CPUT-email rule (incl. lookalike domains), vendor approval, password hashing, duplicate username, wrong password, JWT validity/expiry/forgery, image validation (JPEG/PNG, size limit, bad base64), login rate limiter, production secrets check, app context loads.
- Live checks on the running app: rate limiting (attempts 1-5 returned 401, 6-7 returned 429; a real login still worked); concurrency test found a **real bug** (6 simultaneous mark-sold requests reduced stock by 1 instead of 6), fixed with a row lock and re-tested (50 to 44); 3.8 MB photo accepted, 4.5 MB rejected; duplicate-submit errors return 409.
- Not automated: marketplace, chat, rating and admin flows were tested manually only.

**Deliberate limits and gaps against the brief (be honest about these, see Final Report):**
- **No cart, checkout or payment gateway** (the brief lists "cart & checkout, payment integration"; PayFast/SnapScan not integrated). Trade is arranged in chat and paid in person; the buyer-confirmation step is the trust control.
- **Modular monolith, not microservices**; no Redis cache; no cloud auto-scaling; runs locally.
- **No AI fraud detection or 2FA**; fraud control is reports, moderation, blocking, rate limiting and rating rules.
- **No real email verification** (accounts are auto-verified; email is a stub).
- Login rate limiting is in memory (single server). Photos are stored on local disk.
- Schema is created by Hibernate, not migrations.
- Vendor "business registration" check is manual admin approval.

**Demo accounts:** `Password123!` for thabo_s, lerato_m, aisha_n, sizwe_d, campusprint (vendor), quickfix_it (pending vendor); admin: `admin` / `Admin123!`. Presenter guide: `demo/PRESENTER-GUIDE.md`.

---

## 2. Rules from the brief to respect

- Submit **individual files, not one zip**: (1) Portfolio, one single file; (2) Artefact (this one may be zipped); (3) Video MP4, **10 minutes or less, all 3 members visible and speaking**.
- A **contribution page** is required.
- A **signed plagiarism declaration on the last page**.
- Evidence of **Agile** (sprints, user stories, board, retrospectives, incremental deliverables).
- Stay on the Community Store brief; your ideas must be differentiated.
- Write the documents in your own words and keep your own notes and drafts; markers can ask you to explain anything you submit.

---

## 3. Document-by-document

### 3.1 Final Report (about 6-10 pages)

Outline, in this order (it's the order the brief names):
1. **Background**: the campus problem (fragmented WhatsApp/Facebook trading, no trust, no local identity). 1-2 paragraphs.
2. **Problem statement**: one clear paragraph. Test: could a stranger read it and know who suffers and how?
3. **Project objectives**: 4-6 SMART bullets. *(idea: "CPUT-verified accounts", "buyer-confirmed ratings", "moderation tools", "installable on phones"; give each a measurable target and a date.)*
4. **Scope**: in scope (use the fact sheet's features), out of scope, **limitations** (use the gaps list above and say *why*: time, budget, no merchant account, student project).
5. **Systems analysis**:
   - Requirements: functional (user stories) and non-functional (security, mobile-first, performance, usability). Reuse your Term 2 backlog. Table: ID | Requirement | Priority (MoSCoW) | Implemented? (Yes/Partly/No).
   - Design: one architecture diagram (browser, Spring Boot API, database) and one ER diagram of the 13 tables (README has a diagram you can redraw).
6. **Testing and evaluation**: use the test table below, then a short evaluation: did it meet each objective? what failed and was fixed (the concurrency bug is a good concrete example)? what remains.
7. **Conclusion and recommendations**: 1 paragraph.

Test table template (run each yourselves and fill in Actual and Pass/Fail):

| ID | Scenario | Steps | Expected | Actual | Pass? |
|---|---|---|---|---|---|
| T1 | Student must use CPUT email | Register STUDENT with a gmail address | Rejected with message | | |
| T2 | Student registers with CPUT email | Register with x@mycput.ac.za | Account created | | |
| T3 | Vendor needs approval | Log in as quickfix_it, try to create a listing | Blocked until admin approves | | |
| T4 | Wrong-password lockout | 6 wrong logins for one username | 6th returns "Too many attempts" | | |
| T5 | Create listing with photo | Dashboard, New Listing, add a 3 MB JPEG | Listing shows photo | | |
| T6 | Oversized photo | Add a 5 MB JPEG | Clear error, not saved | | |
| T7 | Chat per listing | Buyer messages seller, seller replies | Both see messages | | |
| T8 | Sale to a named buyer | Seller marks sold to buyer | Listing shows Sold, buyer notified | | |
| T9 | Rating needs confirmed receipt | Buyer tries to rate before confirming | Blocked | | |
| T10 | Rating after confirmation | Buyer confirms then rates 5 stars | Rating saved, seller average updates | | |
| T11 | Cannot rate twice | Rate the same sale again | Rejected (409) | | |
| T12 | Report and moderate | Report a listing; admin reviews | Appears in queue, admin can act | | |
| T13 | Suspension works at once | Admin suspends a logged-in user | User blocked on next request | | |
| T14 | Wishlist alerts | Heart an item; seller drops price | Notification arrives | | |
| T15 | Install as PWA | Chrome install prompt | Installs and opens | | |

### 3.2 Stakeholder Engagement Matrix (2-3 pages)

Start from your **Term 1 stakeholder analysis**. Table template:

| Stakeholder | Role / interest | Influence (H/M/L) | Impact on project | Current engagement (Unaware / Resistant / Neutral / Supportive / Leading) | Desired engagement | Engagement strategy | Communication method and frequency | Owner |
|---|---|---|---|---|---|---|---|---|

Stakeholders to consider *(idea)*: CPUT students (buyers/sellers), student vendors/outside vendors, residents, faculty, SRC/student clubs, campus security/IT department, the lecturer (client/sponsor), your own team, hosting/payment providers. Add a one-paragraph power/interest summary and the Mendelow quadrant each fits.
Prompts: Who can stop this project? Who must adopt it for it to succeed? Which stakeholder's engagement changed since Term 1, and why?

### 3.3 Resource Plan (RACI and Resource Allocation Overview)

**RACI**: rows = deliverables/activities, columns = your 3 members' names with their roles from the brief (Project Manager, Backend, Frontend, QA, Security Specialist, Community Liaison, other). Rule: **exactly one A (Accountable) per row**, at least one R, nobody both blank everywhere.

| Activity | Member 1 (PM) | Member 2 | Member 3 |
|---|---|---|---|
| Project charter and planning | | | | |
| Requirements and user stories | | | | |
| UI/UX wireframes | | | | |
| Backend API and database | | | | |
| Frontend development | | | | |
| Security and fraud controls | | | | |
| Testing and bug tracking | | | | |
| Stakeholder and vendor engagement | | | | |
| Documentation | | | | |
| Demo video | | | | |

**Resource allocation overview** table:

| Resource | Type (people/tool/infra/cost) | Used for | Quantity or hours | Cost (R, or free) | Period |
|---|---|---|---|---|---|

Resources that really exist in this project: three team members; Java 21, Spring Boot, Vue 3, Vite, MySQL/MariaDB, Git and GitHub, VS Code / IntelliJ, Blackboard, free-licence stock photos (credits file in `backend/uploads/products/CREDITS.txt`); local hosting on a laptop (R0). Add real hours per member; a short paragraph on resource conflicts and how you levelled workload.

### 3.4 Risk Management Plan (Risk Register and RAID log)

Plan section (half a page): method, who reviews, how often (the brief says each phase and bi-weekly sprint reviews), scoring scale (e.g. Probability 1-5 x Impact 1-5), thresholds for High/Med/Low.

Risk register template:

| ID | Risk (cause, event, effect) | Category | Prob. | Impact | Score | Response (Avoid/Mitigate/Transfer/Accept) | Action | Owner | Status |
|---|---|---|---|---|---|---|---|---|---|

Candidate risks drawn from this project *(ideas, rate them yourselves)*: fake accounts using someone else's CPUT email (no real email verification); vendor fraud; payment fraud (you chose in-person payment); dev secrets leaking from the repo; brute-force logins; double-submission races on sales (found and fixed); data loss (photos stored on disk, no automated backup); low adoption versus WhatsApp/Facebook; team member unavailable or uneven workload; scope creep against the brief (payments, microservices); deadline compression; hosting/offline failure at demo time.

RAID log template (one table with a Type column):

| ID | Type (Risk / Assumption / Issue / Dependency) | Description | Impact | Owner | Date raised | Status / resolution |
|---|---|---|---|---|---|---|

Real entries you can adapt: Issue: concurrent mark-sold bug found in testing, fixed with row lock. Issue: MariaDB rejected the first lock approach, switched to plain `FOR UPDATE`. Assumption: students own their CPUT emails. Dependency: free stock photos and licence credits; browser Web Push support; MariaDB/MySQL availability.

### 3.5 Quality Management Plan (2-3 pages)

Sections: quality objectives (measurable); standards and conventions (e.g. REST naming, module structure, DTO validation, code reviews via Git); quality planning, quality assurance (process: reviews, retrospectives, CI) and quality control (product: testing, bug tracking); roles; tools; defect management (how found, logged, prioritised, fixed, re-tested); acceptance criteria; metrics and reporting.

| Quality objective | Metric / target | How measured | Frequency | Owner |
|---|---|---|---|---|

Evidence you have: 25 automated tests; CI workflow that builds and tests on every push (`.github/workflows/ci.yml`); manual test table (3.1); a real defect found by testing and its fix; input validation (email domain, image type/size, passwords 8+ chars); acceptance criteria per user story from your backlog.
Prompts: what does "done" mean for a story? Who signs off? What would you automate next?

### 3.6 Transition and Closure Plan (2 pages)

Parts: transition plan (hand-over to a client or next team), training, support, closure activities.

| Item | Detail | Owner | Done? |
|---|---|---|---|

Include *(idea)*: deliverables handed over (source, database scripts, demo seed data, README, DEPLOYMENT.md); setup steps (README steps 0-5); production checklist (set `JWT_SECRET`, DB password, VAPID keys, `SPRING_PROFILES_ACTIVE=prod`, HTTPS, CORS origin, first admin account); known limitations and recommended next steps (payment gateway, email verification, migrations, cloud storage, monitoring); admin training (vendor approval, reports, suspensions, appeals); support and maintenance responsibility; acceptance sign-off; archive repository and documents; release team members; final stakeholder communication; lessons learned session.

### 3.7 Reflection and Lessons Learned

Two parts:
1. **Team lessons learned** table: what went well / what didn't / what we'd change, per area (planning, Agile ceremonies, communication, technical choices, testing, risk, stakeholder work). Link each lesson to evidence (a sprint retrospective, a bug, a change request).
2. **Individual reflections** (half a page each, genuinely personal, different from each other): my role, what I learned, hardest moment, how I handled it, what I'd do differently, a skill I want to improve.
Prompts: Did Agile help? Which practice stuck (backlog, retrospectives, boards)? Where did the plan differ from reality (the brief asked for payments and microservices; why did you scope differently)? What did the concurrency bug teach about testing?

### 3.8 Contribution page

| Member | Student no. | Role | What they did (specific) | Hours or % | Signature |
|---|---|---|---|---|---|

Be specific and honest; the numbers should be consistent with the RACI and the video.

---

## 4. Video checklist (10 minutes or less)

- Run it from `demo/PRESENTER-GUIDE.md` (one presenter drives the demo, all three of you appear on camera at the start and end, 9:30 total). Every member appears on camera and speaks.
- Phone view in Chrome; close the notifications banner first; start with `.\demo\start-demo.ps1`.
- If something breaks, put the demo data back: stop the backend, run `mysql -u root < demo\demo-data-backup.sql` (it recreates the `cputrade` tables with the demo data), then restart the backend.
- Rehearse once with a timer. Export as MP4. Check the file plays and the audio is audible before uploading.
- Open with the problem, show the solution, close with limits and next steps (this shows maturity).

## 5. Final submission checklist

- [ ] Portfolio: one file; has Final Report, Stakeholder Matrix, Resource Plan (RACI + allocation), Risk Plan (register + RAID), Quality Plan, Transition and Closure Plan, Reflection, contribution page, **signed plagiarism declaration last**
- [ ] Names, roles and dates consistent across every document
- [ ] Artefact zip includes `backend/uploads/products/` (photos), `backend/db/` scripts, `demo/`, README, DEPLOYMENT.md; no `node_modules`, no `target`
- [ ] Video MP4 under 10 minutes, all three visible
- [ ] Three separate files uploaded (portfolio, artefact, video), not zipped together
