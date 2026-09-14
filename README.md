# CPUTrade

CPUT's trusted student marketplace — trade textbooks, electronics, and more with
fellow students, safely and on campus.

- **Backend**: Spring Boot 3 (Java 21) + Spring Security + JWT + Spring Data JPA + MySQL
- **Frontend**: Vue 3 + Vite + Vue Router + Pinia + Axios

This repo is being built out incrementally against the Group 18 Term 2 project
management submission (communication plan, backlog, wireframes, risk log). The
first implementation pass covers the **must-have** user stories only:

- US1.1–1.5 — registration, bcrypt password hashing, email verification (stubbed),
  JWT login, admin vendor approval
- US2.1–2.2 — create/edit/delete product listings
- US3.1 — category filter search
- US6.3 — admin removal of fraudulent/abusive listings
- US7.2 — mobile-responsive UI (375px+)

Should-have and could-have stories (in-app chat, ratings, bulletin board, offline
cache, keyword/price search) follow in a later pass.

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
