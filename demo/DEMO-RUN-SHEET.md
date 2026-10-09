# CPUTrade demo run sheet (10 minutes)

Start: `.\demo\start-demo.ps1`, wait ~20 s, open http://localhost:5173 in Chrome
(use the phone view: F12, then the device icon, then iPhone 12 Pro / 390x844).
Click the X on the "Enable notifications" banner before recording.

All demo accounts use password `Password123!` (admin: `admin` / `Admin123!`).
Students: thabo_s, lerato_m, aisha_n, sizwe_d. Vendors: campusprint (approved), quickfix_it (pending).

| Min | Who | What to show | Problem it solves |
|---|---|---|---|
| 0:00 | All | Marketplace home: photos, campus filter, categories, "Just sold" ticker | Fragmented WhatsApp/Facebook selling |
| 1:00 | Member 1 | Search + price filter + "Open to Trade" (swap) + Freecycle (R0) item | Affordability, sustainability |
| 2:00 | Member 2 | Register as a new student: try a gmail address (rejected), then a @mycput.ac.za one | Trust: CPUT-only |
| 3:00 | Member 2 | Create a listing with photos (Dashboard, New Listing) | Selling made simple |
| 4:00 | Member 3 | As thabo_s: open the calculator, message the seller; as lerato_m: reply in My Chats | Direct, safe chat |
| 5:00 | Member 3 | As lerato_m: Mark as Sold, pick sizwe_d. As sizwe_d: Confirm received, then rate 5 stars | Trust: ratings only after buyer confirms receipt |
| 6:30 | Member 4 | Community bulletin board: post a Lost item, filter Found | Community hub |
| 7:30 | Member 4 | Wishlist (heart), notifications, install as PWA | Engagement, mobile-first |
| 8:30 | Member 5 | As a student: Report a listing. As admin: review the report, approve quickfix_it, suspend a user, student appeals | Moderation and fraud control |
| 9:30 | All | Close: what problem it solves, what comes next | |

Reset the demo data at any time: drop and recreate `cputrade_demo`, boot the backend once, run
`db/seed-admin.sql` (change `USE cputrade;` to `USE cputrade_demo;`), `db/seed-demo-data.sql`,
then `node demo/seed-interactions.mjs` (creates the chats, wishlist, and the sold + rated sale).
