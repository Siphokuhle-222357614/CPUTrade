# 08 · Reflection and Lessons Learned: writing guide

**Owner suggestion:** each member writes their own personal reflection; Member 3 compiles the team table. **Length:** team part 1 page, each personal reflection about half a page. **Time:** 25 to 30 minutes.

## Purpose

Show that you can **learn from the project**: what happened, why, and what you will do differently. Markers look for honesty, depth and links to evidence, not for praise. The brief lists "Reflection & Lessons Learned" as a final deliverable.

## Key ideas explained simply

- **Reflection:** thinking back about experience and what you learned from it. Not just "what we did" but "what it meant and what changes".
- **Lessons learned:** specific, reusable knowledge ("do X next time because Y happened").
- **A simple reflection model (Gibbs-style):** What happened? How did I feel? What went well and badly? Why did it happen (analysis)? What did I learn (conclusion)? What will I do next time (action)?

## Part 1: Team lessons learned (a table, 1 page)

| Area | What went well | What did not go well | Lesson / what we would change | Evidence |
|---|---|---|---|---|
| Planning and scope | | | | |
| Agile practice (backlog, sprints, retrospectives) | | | | |
| Communication and teamwork | | | | |
| Technical decisions | | | | |
| Testing and quality | | | | |
| Risk and change management | | | | |
| Stakeholder engagement | | | | |
| Time management and deadlines | | | | |

Fill each row with specific events from *your* project. Link each lesson to evidence (a sprint retrospective, a bug, a change request, a decision).

### Real events in this project you can reflect on *(use only what truly applies to you)*
- **Scope versus the brief:** the brief asked for payments, microservices and AI fraud detection; the team built a trust-focused marketplace instead (verified identity, buyer-confirmed ratings, moderation). What did that decision teach about scoping, honesty and trade-offs?
- **Testing found a real defect:** simultaneous "mark sold" requests corrupted the stock count; testing before users caught it. What does that teach about testing concurrency and about relying only on manual checks?
- **Environment surprise:** the first fix failed because the database was MariaDB, not MySQL; the lesson about matching development and production environments.
- **Clean demo data:** old test data appeared during the demo; the lesson about separating test data from demo data and backing things up.
- **Time pressure at the end:** documents, demo and polishing compressed into the final days; what would you plan differently?
- **Team of three:** doing the work of the brief's six roles; how you shared roles and what suffered.
- **Agile:** which practices stuck (backlog, retrospectives) and which did not.

## Part 2: Individual reflection (one per member, about 250 to 350 words each)

**Each member must write their own, in a personal voice. They should not look alike.** Use this structure:

1. **My role and what I did** (2 to 3 sentences, specific; matches the contribution page).
2. **A highlight:** something I am proud of and why.
3. **A challenge:** the hardest moment, what caused it, how I handled it.
4. **What I learned:** technical skills and project management skills (for example estimating, communicating, risk thinking, Agile).
5. **What I would do differently:** one or two concrete changes.
6. **How I will use this:** a skill to keep improving or a habit to adopt.

Prompts to unlock writing: What surprised you? What did you underestimate? When did the plan stop matching reality? What did you learn from a teammate? How did you feel when something broke or when it worked?

## Part 3: Overall conclusion (3 to 5 sentences, team voice)
What the team achieved, the biggest lesson, and one recommendation for a future team.

## Tone and length tips

- First person for the personal parts ("I"), team voice ("we") for the table and conclusion.
- Be honest about difficulties; admitting a mistake and what you learned from it scores better than perfection.
- Avoid generic lines ("teamwork is important"). Anchor each point to an event.
- Do not exaggerate your own contribution; it must match the contribution page and the video.

## Common mistakes

- Only describing what was built (that belongs in the Final Report).
- Identical reflections for all members.
- No lessons or no changes for next time.
- Hiding problems.

## Checklist

- [ ] Team lessons table with specific, evidence-linked entries
- [ ] One individual reflection per member, each personal
- [ ] Each reflection has a role, highlight, challenge, learning, change, next step
- [ ] Consistent with the contribution page and RACI
- [ ] Short team conclusion
