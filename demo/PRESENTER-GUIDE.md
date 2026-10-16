# CPUTrade Video Presentation Guide

**Target length: 9 minutes 20 seconds. Hard stop: 10 minutes.**

This is a local-app walkthrough for the school presentation. Keep the buyer,
seller, and admin sessions prepared in separate browser profiles before
recording. Do not spend video time on account setup. Use a local demo database,
not production data.

## Before recording

1. From the project root, start the app with `.\demo\start-demo.ps1`.
2. Wait for the backend and frontend windows to finish starting, then open
   `http://localhost:5173`.
3. Prepare separate, already-authenticated buyer, seller, and admin browser
   profiles. Keep the buyer and seller sessions available for the order and
   report hand-offs.
4. Confirm the seeded listings, chat, and photos are visible. Photo files are
   served from `backend/uploads/products/`; MySQL's `product_images` table
   stores their URLs. If a photo is missing, upload a local photo to a demo
   listing before recording or choose a listing whose photo loads.
5. Rehearse the order flow once. Orders are persisted, and
   `reset-live-demo.sql` does **not** delete `marketplace_orders`. For a clean
   repeatable take, restore a fresh local demo database rather than relying on
   that script to erase order history.
6. Close unrelated tabs, silence notifications, and set the browser zoom so
   the page fits the recording.

## Timed run of show

| Time | Navigate and show | Say |
|---|---|---|
| **0:00-0:25** | Start on **Home** (`/`). | "CPUTrade is a campus marketplace for CPUT students to buy, sell, swap, or give away items, with communication and moderation kept in one place." |
| **0:25-1:35** | On **Home**, show category pills, **Search**, **Campus**, price filters, and **Open to Trade**. Search for a seeded item, apply one filter, then clear it. | "Students can narrow listings by what they need and where they study, instead of searching scattered group chats." |
| **1:35-2:20** | Open a listing card at `/products/:id`. Show photos, condition, price, seller information, and **Message Seller** / **Buy this item**. | "The listing page brings the item and seller details together. Photos load from the backend; the database stores each photo URL rather than embedding images in the frontend." |
| **2:20-2:55** | Click **+ Sell** to show the listing form at `/products/new`. Point out the listing and photo fields; do not submit a duplicate listing during the presentation. | "A seller can create a listing with its details and photos. We can manage listings locally without a hosting or deployment step." |
| **2:55-3:45** | Click **Chats**. Open a prepared conversation at `/chats/:id`; show the message thread and safe meetup suggestions. Avoid sending a rehearsal message unless needed. | "Buyer and seller can communicate inside the app and discuss a public campus meetup without putting contact details in the listing." |
| **3:45-5:35** | In the buyer profile, open a listing and choose **Buy this item**. Complete the simulated checkout. Switch to the seller profile, click **Orders**, mark the order ready for hand-off, then switch back to the buyer's **Orders** and confirm receipt. | "Checkout records a simulated order only: it does not collect card details or charge money. Stock is reserved for the order, and the buyer confirms receipt before rating the seller." |
| **5:35-6:10** | Click **Board** and show a bulletin post, especially a Lost & Found item if one is seeded. | "The campus board also supports community notices and Lost & Found posts." |
| **6:10-7:35** | As the buyer, use **Report** on a suitable listing and submit a short, truthful demo reason. Switch to the admin profile, click **Admin**, scroll to **Reports Queue**, and review or dismiss the report. Point out **Recent Admin Actions**. | "Reports go to an admin queue. Admin decisions are recorded in the action history so moderation has an audit trail." |
| **7:35-8:20** | Show **Wishlist** and, if time permits, the avatar menu's **Dashboard**. | "Students can save items and see seller activity, including listing and sales information, from their own account areas." |
| **8:20-9:20** | Return to **Home**. Briefly resize to a narrow mobile viewport, then finish on the marketplace. | "CPUTrade is built as a responsive local-first application. The key flows we showed are listings, chat, simulated orders, and admin moderation. Thank you." |

## Keep the demo safe and on time

- If the seeded listing or photo is unavailable, switch to a prepared listing;
  do not invent a successful result.
- Do not enter real payment information. There is no live payment processing.
- Only submit truthful test data in the local demo database.
- Order history remains after the supplied reset script runs. Restore a fresh
  local demo database if rehearsals make the order list confusing.
- If time is short, skip the listing-form preview and dashboard; keep the
  listing details, order lifecycle, and report-to-admin flow.
- Stop by **9:20** to leave a buffer before the 10-minute limit.

For hands-on acceptance testing outside the video, use
[LOCAL-TESTING-CHECKLIST.md](./LOCAL-TESTING-CHECKLIST.md).
