-- OPTIONAL demo data for local development: a few users, listings and
-- bulletin posts so the marketplace isn't empty on first run.
--
-- Run this AFTER the backend has started at least once (so every table
-- exists) — same as seed-admin.sql. Never run it against production.
--
-- Every account below logs in with password: Password123!
--
--   thabo_s      STUDENT   (Bellville)
--   lerato_m     STUDENT   (District Six)
--   campusprint  VENDOR    already approved — can sell straight away
--   quickfix_it  VENDOR    NOT approved yet — log in as admin to approve it
--
-- Safe to run more than once: users are INSERT IGNOREd and every listing /
-- post is skipped if the same author already has one with that title.
-- Listings have no photos yet. Uploaded files live under backend/uploads/,
-- while MySQL stores their URLs in product_images. Add photos through the app.

USE cputrade;

-- ---------------------------------------------------------------- users
INSERT IGNORE INTO users (username, email, campus_handle, password_hash, role, verified, vendor_approved, account_status, created_at)
VALUES
  ('thabo_s',     'thabo_s@mycput.ac.za',        'thabo_s@cputmarket.ac.za',     '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'STUDENT', true, true,  'ACTIVE', NOW()),
  ('lerato_m',    'lerato_m@mycput.ac.za',       'lerato_m@cputmarket.ac.za',    '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'STUDENT', true, true,  'ACTIVE', NOW()),
  ('campusprint', 'hello@campusprint.example',   'campusprint@cputmarket.ac.za', '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'VENDOR',  true, true,  'ACTIVE', NOW()),
  ('quickfix_it', 'support@quickfix.example',    'quickfix_it@cputmarket.ac.za', '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'VENDOR',  true, false, 'ACTIVE', NOW());

-- ------------------------------------------------------------- listings
-- Columns: title, description, price, category, item_condition, campus,
--          quantity, open_to_swap, swap_preferences, seller username
INSERT INTO products (seller_id, title, description, price, category, item_condition, campus,
                      quantity, open_to_swap, swap_preferences,
                      is_active, sold, buyer_confirmed, view_count, created_at, updated_at)
SELECT u.id, s.title, s.description, s.price, s.category, s.item_condition, s.campus,
       s.quantity, s.open_to_swap, s.swap_preferences,
       true, false, false, 0, NOW(), NOW()
FROM (
  SELECT 'thabo_s' AS seller, 'Calculus: Early Transcendentals (8th ed.)' AS title,
         'Used for Maths 1. A few highlighted pages, otherwise clean.' AS description,
         350.00 AS price, 'TEXTBOOKS' AS category, 'GOOD' AS item_condition, 'BELLVILLE' AS campus,
         1 AS quantity, false AS open_to_swap, NULL AS swap_preferences
  UNION ALL SELECT 'thabo_s', 'Casio fx-991EX scientific calculator',
         'Works perfectly, comes with its cover.',
         250.00, 'ELECTRONICS', 'LIKE_NEW', 'BELLVILLE', 1, true, 'A Programming 1 textbook'
  UNION ALL SELECT 'lerato_m', 'Free: Intro to Accounting notes',
         'Printed and bound first-year notes. Free to a good home (Freecycle).',
         0.00, 'TEXTBOOKS', 'FAIR', 'DISTRICT_SIX', 1, false, NULL
  UNION ALL SELECT 'lerato_m', 'CPUT hoodie (size M)',
         'Navy, worn twice.',
         180.00, 'CLOTHING', 'LIKE_NEW', 'DISTRICT_SIX', 1, false, NULL
  UNION ALL SELECT 'campusprint', 'A4 printing & binding',
         'Assignment printing and spiral binding, collected on campus. Price is per bound document.',
         45.00, 'SERVICES', 'NEW', 'BELLVILLE', 50, false, NULL
) AS s
JOIN users u ON u.username = s.seller
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.seller_id = u.id AND p.title = s.title);

-- ------------------------------------------------ bulletin board (Lost & Found)
INSERT INTO bulletin_posts (author_id, title, body, type, resolved, created_at)
SELECT u.id, s.title, s.body, s.type, false, NOW()
FROM (
  SELECT 'lerato_m' AS author, 'Lost: blue student card holder' AS title,
         'Lost near the District Six library on Monday. Please message me if found!' AS body, 'LOST' AS type
  UNION ALL SELECT 'thabo_s', 'Found: set of keys at Bellville cafeteria',
         'Three keys on a red lanyard. Handed in at the SRC office.', 'FOUND'
  UNION ALL SELECT 'thabo_s', 'Study group for Programming 1 exam',
         'Meeting in the IT building every Thursday at 14:00 — all welcome.', 'GENERAL'
) AS s
JOIN users u ON u.username = s.author
WHERE NOT EXISTS (SELECT 1 FROM bulletin_posts b WHERE b.author_id = u.id AND b.title = s.title);
