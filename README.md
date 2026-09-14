# CPUTrade

CPUT's trusted student marketplace — trade textbooks, electronics, and more with
fellow students, safely and on campus.

- **Backend**: Spring Boot 4 (Java 21) + Spring Security + JWT + Spring Data JPA + MySQL
- **Frontend**: Vue 3 + Vite + Vue Router + Pinia + Axios

Built out against the Group 18 Term 2 project management submission
(communication plan, backlog, wireframes, risk log). All 24 backlog stories are
implemented:

- **Must-have** — US1.1–1.5 (registration, bcrypt hashing, email verification
  stubbed, JWT login, admin vendor approval), US2.1–2.2 (create/edit/delete
  listings), US3.1 (category filter), US6.3 (admin listing removal), US7.2
  (mobile-responsive UI, 320px+)
- **Should-have** — US3.2–3.3 (keyword + price-range search), US4.1–4.2
  (in-app chat with a safe-meetup-location dropdown), US6.1–6.2 (seller
  ratings)
- **Could-have** — US2.3 (Freecycle badge), US2.4 (listing view counts),
  US4.3 (in-app notifications), US5.1–5.3 (campus bulletin board), US7.1
  (offline cache for the marketplace feed and last-viewed listing)

Email delivery (US1.3, vendor-approval, listing-removed) is stubbed — accounts
auto-verify and emails are logged, not sent; wiring in a real provider is a
drop-in change behind `EmailNotifier` (see `user/notification/`).

See [DEPLOYMENT.md](DEPLOYMENT.md) for running this anywhere but localhost.

## Project layout

```
backend/    Spring Boot API (Maven, own mvnw wrapper)
frontend/   Vue 3 SPA (Vite)
```

## Prerequisites

- JDK 21
- Node.js 18+ (tested on v22)
- MySQL 8.x running locally

## Getting started

See `backend/README.md` and `frontend/README.md` (added once each is scaffolded)
for run instructions. In short:

```bash
# 1. MySQL — create schema/user once (see backend setup notes)

# 2. Backend
cd backend
./mvnw.cmd spring-boot:run   # http://localhost:8080

# 3. Frontend
cd frontend
npm install
npm run dev                  # http://localhost:5173
```
