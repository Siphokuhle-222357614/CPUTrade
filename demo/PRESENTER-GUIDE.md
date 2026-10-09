# CPUTrade: presenter guide (one presenter, 9 min 30 s)

One person drives the demo and does the talking. Target finish: **9:30**, leaving 30 seconds under the 10-minute limit.
Lines in quotes are *suggestions*; say them in your own words, don't read them out.

**Brief requirement:** the video rules say *all group members should participate and be visible*. So the other two members
appear on camera at the **start** (0:00-0:30) and the **end** (9:00-9:30) and each says a sentence. The main presenter does everything in between.

---

## Before you record (10 minutes)

1. Start the app: `.\demo\start-demo.ps1` (wait about 20 seconds). Open http://localhost:5173. The app now uses the normal `cputrade` database, which holds only the demo data.
2. Use **three browser windows** so you never log in and out on camera. Put each in phone view (F12, device icon, iPhone 12 Pro 390x844):

| Window | Logged in as | Used for |
|---|---|---|
| Chrome (normal) | `thabo_s` (buyer) | marketplace, chat, buying, reporting |
| Chrome Incognito | `lerato_m` (seller) | replying to chat, marking sold |
| Edge | `admin` (password `Admin123!`) | admin moderation |

Student passwords are `Password123!`.

3. In every window, **click the X on the "Enable notifications" banner**.
4. Rehearse once with a timer. After a rehearsal, put the demo back with `mysql -u root < demo\reset-live-demo.sql`, then refresh every window.
5. Record screen plus sound, with a small webcam box so all three of you can be seen at the start and end.

**If something breaks:** say "let me refresh that", press F5, keep going. If the data looks wrong, run the reset file. If the whole database is damaged: stop the backend, run `mysql -u root < demo\demo-data-backup.sql`, restart the backend.

---

## Timeline

| Time | Segment |
|---|---|
| 0:00-0:30 | Intro: all three on camera |
| 0:30-0:55 | The problem |
| 0:55-1:50 | Marketplace tour |
| 1:50-2:25 | Listing detail and selling |
| 2:25-2:55 | Trust: CPUT-only registration |
| 2:55-3:30 | Chat between buyer and seller |
| 3:30-5:10 | The verified sale: sold, confirm receipt, rating |
| 5:10-5:35 | Community bulletin board |
| 5:35-6:35 | Safety: report and admin moderation |
| 6:35-7:10 | Under the hood: security and testing |
| **7:10-9:10** | **What is not in the system, and why (2 minutes, scripted below)** |
| 9:10-9:30 | Close (the other two on camera again) |

Running late? Cut: the price filter in the tour, the "Remove" point in admin, the bulletin post. **Never cut the 2-minute "not in the system" segment**: there is no live Q&A in the video, so this is where you answer the examiner's questions before they are asked.

---

## Segment by segment

**0:00-0:30, intro** *(all three on camera)*
- Each member says their name and role in one sentence.
- Main presenter: "CPUTrade is a mobile-first campus marketplace where CPUT students and approved local vendors buy, sell and swap safely."

**0:30-0:55, the problem** *(still on camera or title screen)*
- Three points: students trade on WhatsApp and Facebook groups; nothing is verified so scams are easy; there is no single place built for campus life (textbooks, res items, Lost and Found).
- "So we built a marketplace where identity is real and every sale is verified."

**0:55-1:50, marketplace tour** *(Chrome, `thabo_s`, Home)*
1. Point at the **Home screen**: the live "Just sold" ticker and the real photos.
2. Tap a **category pill** (Electronics), then the **Campus filter** (Bellville): "Five CPUT campuses, so you see what is near you."
3. Type "calculator" in **Search**, set a max price, then clear it.
4. Tick **Open to Trade**: "Sellers can offer a swap, for example this calculator for a Programming textbook."
5. Show the **R0 listing**: "Price zero shows a Freecycle badge, which supports sustainability."

**1:50-2:25, listing detail and selling**
1. Open the **calculator listing**: photos, condition, campus, seller rating, people watching, **Share to WhatsApp**.
2. Open **+ New Listing** (do not submit): "Listing takes under a minute: up to 6 photos, up to 4 MB each, quantity, condition and campus."

**2:25-2:55, CPUT-only registration** *(new tab, `/login`, Register)*
1. Choose Register, role Student, email `test@gmail.com`: it is **rejected**. "Students must use a CPUT email."
2. Switch the email to `newstudent@mycput.ac.za` (it would be accepted).
3. "Vendors can use any email but cannot sell until an admin approves them. That is our answer to fake businesses."

**2:55-3:30, chat** *(Chrome `thabo_s`, then Incognito `lerato_m`)*
1. As `thabo_s`: Chats, open the Red Kanken backpack conversation, send "Can we meet at the library at 13:00?"
2. Point out read receipts, the typing indicator and safe meet-up suggestions.
3. Switch to Incognito `lerato_m`: Chats, reply "Yes, see you there."
4. "Every chat is tied to a listing, so there is a record."

**3:30-5:10, the verified sale: the key moment**
1. As `lerato_m`: open the **Red Kanken backpack**, click **Mark as Sold**, pick **thabo_s**. "She can only choose someone who actually messaged her about this item."
2. Switch to `thabo_s`: open the listing. He sees *"Confirm you received it to unlock leaving them a rating."* Click **Confirm I received this**.
3. The **rating form** appears: choose **5 Excellent**, add a comment, submit.
4. Show the seller's updated rating.
5. "A seller cannot rate their own sale. A buyer cannot rate before confirming receipt, and cannot rate twice. That is how we stop fake reviews."

**5:10-5:35, community bulletin board** *(Chrome, Board)*
- Show Lost and Found posts and the filters. "It is more than a shop: events, study groups and Lost and Found make it a community hub."

**5:35-6:35, safety and admin** *(Chrome `thabo_s`, then Edge `admin`)*
1. As `thabo_s`: open a listing that is not his, click **Report**, choose **Scam / fraud**, submit.
2. In Edge as **admin**, open **Admin**. Show the **Reports Queue** and review or dismiss the report.
3. In **Pending Vendor Requests**, click **Approve** on `quickfix_it`.
4. Point at **Manage Students and Vendors**: Suspend. "Suspension works immediately, even for someone already logged in, and they can appeal."
5. Point at **All Listings (Moderation)**: Remove.

**6:35-7:10, under the hood** *(talk over the app)*
- Tech: Vue 3 installable mobile app (PWA) with push notifications, Spring Boot API, MySQL database.
- Security: bcrypt password hashing, login tokens that expire after 24 hours, role-based permissions, login rate limiting (5 wrong passwords locks that login for 15 minutes), images checked by content, not file name.
- Quality: **25 automated tests** pass. Our concurrency test found a **real bug**: six simultaneous sales only reduced stock by one. We fixed it with a database lock and re-tested. "Testing found it before users did."

**7:10-9:10, what is not in the system, and why (2 minutes)**

*Why this segment exists:* the brief mentions payments, escrow, AI fraud detection, two-factor login and microservices. We chose not to build them, so we say so, with the reasons, before anyone asks. On screen: stay on the Home screen, or show the "brief versus built" table in Appendix A.

Say it in your own words. Suggested script (about 275 words, about 2 minutes). Each paragraph is one topic with its time budget:

1. **Opening (15 s).** "Before we finish, here is what is deliberately not in this version, and why. Being clear about limits is part of building responsibly."
2. **Payments and escrow (30 s).** "We did not build a cart or card payments. That was a decision, not an oversight. With no payments, CPUTrade never holds card details, so there is no card-data breach risk and no payment compliance burden. Instead, buyer and seller agree in chat and pay in person, and the buyer confirmation step stops fake reviews. It is not escrow, because we hold no money. A hosted PayFast checkout is our first next step, and real escrow comes with it."
3. **Fraud detection and two-factor login (25 s).** "We did not build AI fraud detection or two-factor login. With no transaction history there is nothing meaningful to train a model on, and with no money moving there is no high-value purchase for two-factor to protect. Fraud is covered by rule-based controls you have seen: CPUT-only emails, vendor approval, reports, suspensions, rate limiting, and ratings that need a confirmed receipt."
4. **Email verification (20 s).** "We check that a student uses a CPUT email domain, but we do not yet send a verification link, so someone could use another person's address. Reports and suspensions contain that risk, and email verification is next."
5. **Architecture and scale (20 s).** "It is a modular monolith, not microservices, and it runs locally rather than on cloud auto-scaling or Redis. For a team of three and a campus-sized user base that is simpler and more reliable. The API is stateless, so it can scale out, and the modules can be split into services later."
6. **Privacy and testing (10 s).** "We have no account deletion or privacy policy yet, and no independent penetration test. Those are on the roadmap before any public launch."

Safe words: say *buyer-confirmed receipt* (not escrow), *rule-based controls* (not AI), *modular monolith* (not microservices), *students must use a CPUT email* (not verified students). The full reasons and security evidence are in the appendix, if you want to rehearse deeper answers.

**9:10-9:30, close** *(the other two members back on camera)*
- Presenter: "Our next steps are email verification, a payment gateway, two-factor login for high-value purchases, and cloud storage."
- Member 2: one short sentence on what they contributed or learned. Member 3: the same.
- Presenter: "CPUTrade makes campus trading affordable, sustainable and safe. Thank you."

---

## Feature cheat sheet

| Area | What it does |
|---|---|
| Accounts | Student (CPUT email only), Vendor (admin approval), Admin |
| Listings | Up to 6 photos (4 MB each), quantity, condition, 5 campuses, Freecycle R0, swap |
| Discovery | Search, category, campus, price, "Open to Trade", recently-sold ticker |
| Chat | Per listing, read receipts, typing, last seen, edit/delete, meet-up suggestions |
| Trust | Seller picks the buyer; buyer confirms receipt; then one rating per sale; Verified Seller badge |
| Engagement | Wishlist (price-drop and sold alerts), saved searches, push notifications, seller dashboard |
| Safety | Report, block, admin moderation, suspensions with appeals |
| Community | Bulletin board with Lost and Found, WhatsApp share |
| Platform | Installable on phones (PWA), offline cache, works down to 320 px wide |
| Security | bcrypt, JWT, roles, rate limiting, safe image checks, production secrets check |
