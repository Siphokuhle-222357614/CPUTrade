# 07 · Transition and Closure Plan: writing guide

**Owner suggestion:** Member 3. **Length:** about 2 pages. **Time:** 25 to 30 minutes.

## Purpose

Show how the system and knowledge **pass to whoever runs it next** (transition) and how the project is **formally finished** (closure). The brief lists "Transition & Closure Plan" as a final deliverable.

## Key ideas explained simply

- **Transition (handover):** moving from "the project team builds it" to "someone else runs and supports it". Covers handover items, training, support and go-live.
- **Closure:** the formal end: confirm everything was delivered, get acceptance, release resources, archive, record lessons.
- **Acceptance criteria:** what the client must see to agree the project is complete.
- **Post-implementation review:** looking back to check benefits and lessons.

## Sections to write

### 1. Purpose and scope (2 to 3 sentences)
Who is receiving the system (for a student project: the lecturer and, in a real roll-out, CPUT or the student community) and what is being handed over.

### 2. Deliverables handed over (table)

| Deliverable | Where it is | Who receives it | Status |
|---|---|---|---|
| Working application (web app and API) | Project folder / zip | Lecturer | |
| Source code and history | Git repository | Lecturer / next team | |
| Database scripts and demo data | `backend/db/`, `demo/demo-data-backup.sql` | | |
| Setup guide | `README.md` | | |
| Deployment guide | `DEPLOYMENT.md` | | |
| Demo video (MP4) | Submission | Lecturer | |
| Portfolio documents | Submission | Lecturer | |
| Demo accounts and credentials | In the handover note (never real passwords) | | |
| Photo credits | `backend/uploads/products/CREDITS.txt` | | |

### 3. Transition approach
Describe how someone could take over and run it. Facts you can use:
1. **Set up:** install Java 21, Node, MySQL; create the database; start the backend and frontend (README steps 0 to 5).
2. **Production readiness checklist** (from DEPLOYMENT.md): set a real `JWT_SECRET`, database password and push (VAPID) keys; run with `SPRING_PROFILES_ACTIVE=prod` (the app refuses to start with development secrets); configure the allowed web address (CORS); serve over HTTPS; create the first admin account; point the frontend to the real API address.
3. **Hosting:** the app is not yet hosted; choose a host for the API, a managed database and a static host for the front end; move photo storage to cloud storage before using several servers.
4. **Data:** demo data can be loaded for training; real data starts empty.
5. **Go-live approach:** pilot on one campus first (for example Bellville), gather feedback, then widen.

### 4. Training and support
- **Admins:** how to approve vendors, review reports, remove listings, suspend and reinstate accounts, handle appeals.
- **Users:** short guide or video on listing, chatting, confirming receipt and rating, reporting.
- **Support:** who answers questions, how problems are reported, response expectations. For a student project, say who maintains it after submission, or state that there is no ongoing support.
- **Maintenance:** backups of the database and photos; updating dependencies; reviewing reports; monitoring.

### 5. Known limitations and recommended next steps (table)

| Item | Why it matters | Recommended next step |
|---|---|---|
| SMTP delivery is optional | New students cannot verify if configured delivery is unavailable | Keep local log delivery for development; configure and test SMTP when outbound email is required |
| No real payment processing / escrow | Checkout records a simulated order only; no money is charged or held | Consider a provider-hosted gateway only if real payments become an approved requirement |
| No two-factor login | Needed once money moves | Add 2FA for high-value purchases |
| Photos on local disk | Lost on some hosts | Cloud object storage |
| Rate limiter is single-server | Needs shared store when scaled | Shared store or proxy rule |
| Database schema made by Hibernate | Changes not reviewable | Move to Flyway migrations |
| No account deletion or privacy policy | Data protection (POPIA) | Policy, retention rule, deletion |
| No independent penetration test | Unknown weaknesses | Test before public launch |

### 6. Closure activities (checklist table)

| Closure activity | Done? | Owner | Date |
|---|---|---|---|
| All deliverables submitted and checked against the brief | | | |
| Acceptance / sign-off obtained (lecturer) | | | |
| Final demo and video completed | | | |
| Open defects and risks documented with next steps | | | |
| Code and documents archived (repository tagged or backed up) | | | |
| Team members released and workload ended | | | |
| Third-party material acknowledged (photo credits, libraries) | | | |
| Lessons-learned session held | | | |
| Final communication to stakeholders sent | | | |

### 7. Closure criteria and sign-off
State the conditions for "project complete" (deliverables submitted; "Must" requirements delivered or limitations documented; demo accepted). Add a small sign-off block:

| Name | Role | Signature | Date |
|---|---|---|---|
| | Project manager | | |
| | Lecturer / assessor | | |

### 8. Post-project review (3 to 5 sentences, your words)
Did the project meet its objectives? Which benefits could be measured later (number of users, successful sales, reports resolved)? Link to the Reflection document.

## Common mistakes

- Only technical instructions with nothing on training, support or closure.
- No limitations or next steps.
- Putting real passwords or secrets into the document.
- Skipping the sign-off and lessons-learned parts.

## Checklist

- [ ] Handover deliverables table
- [ ] Setup and production checklist
- [ ] Training, support and maintenance
- [ ] Limitations and next steps
- [ ] Closure checklist, criteria and sign-off
