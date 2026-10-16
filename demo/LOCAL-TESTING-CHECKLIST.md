# CPUTrade Local Testing Checklist

Use this checklist to manually verify the application on a local machine.
It intentionally excludes login and registration tests. Have already
authenticated buyer, seller, and admin sessions prepared in separate browser
profiles; test the features below from those sessions.

## Safe test setup

- [ ] Use a local, disposable demo database. Do not use production data.
- [ ] Start the backend and frontend using `.\demo\start-demo.ps1` from the
  repository root, or use the project's normal local startup instructions.
- [ ] Wait for both services to start and open `http://localhost:5173`.
- [ ] Prepare separate browser profiles for buyer, seller, and admin actions.
- [ ] Confirm at least one active listing, one chat, and listing photos are
  available. If sample photos are missing, upload a local image to a demo
  listing before testing image display.
- [ ] Keep in mind the demo uses the normal local `cputrade` database.
  `reset-live-demo.sql` does not remove `marketplace_orders`; use a fresh local
  database backup when you need to clear persistent order history.

## Marketplace discovery and listing details

- [ ] Open **Home** (`/`) and confirm active listings load without errors.
- [ ] Select a category pill and verify only matching listings remain.
- [ ] Search by a known listing keyword; clear the search and verify the full
  result set returns.
- [ ] Filter by **Campus**, **Min price (R)**, and **Max price (R)**. Verify
  matching results and the empty-results state when no listing matches.
- [ ] Enable **Open to Trade** and verify swap-enabled listings are shown.
- [ ] Open a listing at `/products/:id`. Verify title, description, condition,
  price/free badge, campus, quantity, seller details, and available actions.
- [ ] Open and navigate all photos for a listing with multiple images.
- [ ] Verify sold or inactive listings cannot be purchased.
- [ ] Check that the listing page is usable at desktop and narrow mobile widths.

## Seller listings and photo persistence

- [ ] As the seller, open **+ Sell** (`/products/new`) and verify required
  listing fields and photo selection are available.
- [ ] Create a uniquely named test listing with a valid local image. Verify it
  appears on **Home** and its detail page shows the photo after a full reload.
- [ ] Edit the test listing, update a field, and verify the saved value remains
  after reload.
- [ ] Remove a photo through the listing editor. Reload the listing and verify
  the removed photo is no longer displayed.
- [ ] Delete the test listing when finished, if the UI offers that action.
- [ ] Verify the image path/architecture is correct: image files are served
  from `backend/uploads/products/`, while MySQL `product_images` stores the
  image URLs. The frontend should render the image URLs returned by the API,
  not contain listing image files or listing-specific hard-coded image URLs.

## Chat and campus board

- [ ] From a listing, choose **Message Seller** and verify the relevant
  conversation opens or is created.
- [ ] Send a short test message. Switch to the other prepared profile and
  verify the message is present in **Chats** (`/chats`) and the conversation.
- [ ] Verify safe meetup suggestions are available in the conversation.
- [ ] Open **Board** (`/board`) and verify posts load.
- [ ] If available, create a clearly marked test post and verify it appears;
  remove it afterward if the UI allows.
- [ ] Verify Lost & Found content is identifiable and its basic post flow is
  usable.

## Wishlist, dashboard, and notifications

- [ ] Add an active listing to **Wishlist** and verify it appears at
  `/wishlist`.
- [ ] Remove it from the wishlist and verify it disappears.
- [ ] Open the avatar menu and go to **Dashboard**. Check seller listing,
  view, sales, rating, and unread-chat information against the prepared data.
- [ ] Open **Notifications** (`/notifications`) and verify the page loads and
  existing notifications can be read or dismissed as offered.
- [ ] If testing price-drop or sold alerts, use a disposable listing and
  confirm the alert after changing its price or availability.

## Simulated order lifecycle and stock

Use different disposable listings for the cancellation and receipt paths so
the outcomes do not conflict.

- [ ] As the buyer, open an active listing and choose **Buy this item**.
- [ ] Complete checkout and verify an order is created in **Orders**
  (`/orders`). Confirm no card details are requested and no real payment is
  taken; checkout is simulated.
- [ ] Verify the order is associated with the selected listing and buyer, and
  that the seller can see it in their **Orders**.
- [ ] As the seller, mark the order ready for hand-off and verify the updated
  status is visible to both parties.
- [ ] On a second disposable order, cancel before confirming receipt. Verify
  the order is cancelled and the reserved quantity is restored.
- [ ] On the first order, confirm receipt as the buyer. Verify the order
  reaches its completed state and the buyer can rate the seller only after
  receipt is confirmed.
- [ ] Submit a rating and verify the updated seller rating is displayed.
- [ ] Reload both order histories and verify order statuses remain correct.

## Reporting and administration

- [ ] As a student, report a suitable listing using **Report** and a truthful
  test reason.
- [ ] As the admin, open **Admin** (`/admin`) and find the report in **Reports
  Queue**.
- [ ] Mark the report reviewed or dismiss it. Verify the resulting status and
  a corresponding entry in **Recent Admin Actions**.
- [ ] Verify a prepared student session cannot access admin-only actions or
  admin history. This checks role authorization without testing login.
- [ ] If the local dataset includes a pending vendor request or appeal, verify
  it appears in its corresponding admin section; do not approve or reject
  records unless that change is part of the test.

## Responsive and offline behavior

- [ ] At a narrow viewport (320-375 px), verify navigation, listing cards,
  listing details, chat, and order actions fit without unusable horizontal
  overflow.
- [ ] At a desktop viewport, verify page content and navigation remain
  readable.
- [ ] Visit **Home** and a listing while online, then use browser developer
  tools to simulate offline mode. Verify the previously cached marketplace
  feed and last-viewed listing remain available as designed.
- [ ] While offline, verify network-dependent actions clearly report that they
  need a connection; do not expect chat, checkout, or admin changes to work
  offline.

## Automated regression checks

Run from the relevant project directory:

```powershell
# Frontend unit tests
Set-Location frontend
npm test

# Frontend production build
npm run build

# Browser end-to-end tests
npm run test:e2e

# Backend tests
Set-Location ..\backend
.\mvnw.cmd test
```

Record the date, browser/device, test data used, pass/fail result, and any
reproducible failure. Use a fresh local database when repeated order tests
would otherwise leave old orders in the demo history.
