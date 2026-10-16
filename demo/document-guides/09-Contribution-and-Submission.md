# 09 · Contribution page, plagiarism declaration and submission: guide

**Owner suggestion:** Member 1 assembles; every member checks their own entry and signs. **Time:** 20 to 30 minutes at the end.

## 1. What must be submitted (from the brief)

Three items, submitted **as separate files (do not zip everything together)**:

| # | Item | Format | Notes |
|---|---|---|---|
| 1 | **Comprehensive Project Portfolio** | One single file (PDF or Word) | Contains all documents below, a contribution page, and the signed plagiarism declaration as the **last page** |
| 2 | **Artefact: the working application** | A zip is allowed | Must be runnable and complete |
| 3 | **Video demonstration** | MP4, 10 minutes or less | **All group members participate and are visible** |

**Contents of the portfolio, in this order** (matches the brief):
1. Final Report
2. Stakeholder Engagement Matrix
3. Resource Plan (RACI chart and Resource Allocation Overview)
4. Risk Management Plan (Risk Register and RAID log)
5. Quality Management Plan
6. Transition and Closure Plan
7. Reflection and Lessons Learned
8. Contribution page
9. Signed plagiarism declaration (last page)

## 2. Contribution page

The brief says "please include a page for contribution". Make it a clear table. **Be specific and honest.** It must match the RACI, the reflections and the video.

| Member | Student number | Role(s) | Specific contributions | Estimated contribution (%) | Signature |
|---|---|---|---|---|---|
| | | | | | |
| | | | | | |
| | | | | | |

**How to fill "specific contributions":** name real work, for example features or modules built, documents written, testing done, demo sections presented, meetings led. Avoid "helped with everything". The percentages should add up to 100 and reflect reality (equal is fine if true).
Add 2 or 3 lines at the bottom: how the team collaborated (tools, meetings, version control) and how disputes or gaps in workload were handled.

## 3. Plagiarism declaration

- The brief says a **signed plagiarism declaration is mandatory** and must be the last page. Use the **department's official declaration form** if you were given one (check Blackboard); otherwise ask the lecturer which wording to use.
- Each member signs and dates.
- **Read it before you sign.** The brief states plagiarism, including AI-generated content, can lead to zero marks and disciplinary action. Sign only if the work is yours and your sources are acknowledged.
- Acknowledge sources: course textbook, framework documentation, photo credits (see `backend/uploads/products/CREDITS.txt`), any templates provided by the department.

## 4. Assembling the portfolio (checklist)

1. Create a cover page: title, course code PRM370/371/372S, group name, member names and student numbers, lecturer, date.
2. Add a contents page with page numbers.
3. Put the documents in the order above; start each on a new page with a clear heading.
4. Use the same fonts, heading styles and table style throughout.
5. Check consistency: member names and roles, feature names, dates, and current test evidence (55 backend tests, 27 frontend unit tests and 8 Playwright browser tests passing; the backend suite includes 4 order lifecycle tests and 2 moderation/audit tests), and other project facts.
6. Number the pages; export to PDF; check that tables and images are not cut off.
7. Check the file size (the submission system may have a limit).
8. Name files clearly, for example `GroupName_Portfolio.pdf`, `GroupName_Artefact.zip`, `GroupName_Demo.mp4`.

## 5. The artefact zip

**Include:**
- `backend/` (without `target/`), `frontend/` (without `node_modules/` and `dist/`)
- `backend/uploads/products/` (**the demo photos are not in Git, so add them**)
- `backend/db/` scripts and `demo/` folder (guides, backup, reset script, screenshots)
- `README.md` and `DEPLOYMENT.md`

**Exclude:** `node_modules`, `target`, `dist`, `.git` (optional), any real secrets or passwords.

**Add a short "how to run" note in the zip** (a few lines pointing to README steps 0 to 5 and the demo accounts: students use `Password123!`, admin `admin` / `Admin123!`). Restore the demo data with `demo/demo-data-backup.sql`.

**Test it:** if you can, unzip it in a fresh folder (or on another laptop) and follow your own README to make sure it starts.

## 6. The video (recap)

- 10 minutes or less, MP4, sound clear. Follow `demo/PRESENTER-GUIDE.pdf` (9:30 plan).
- **All three members must appear and speak** at least briefly (intro and close).
- Show the working system, the key features, and the honest "what is not in the system and why" segment.
- Play the finished file once before uploading, and check the length.

## 7. Final checklist before upload

- [ ] Portfolio is **one file** with all required documents in order
- [ ] Contribution page filled and consistent
- [ ] Plagiarism declaration signed by all three and is the **last page**
- [ ] Artefact zip includes photos and instructions, excludes `node_modules` and `target`
- [ ] Video is MP4, 10 minutes or less, all members visible
- [ ] Three separate files uploaded, not one big zip
- [ ] Every document uses the same names, roles and facts
- [ ] Everything written in your own words and sources acknowledged
