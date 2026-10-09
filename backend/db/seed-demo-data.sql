-- DEMO data for presentations: realistic users, listings WITH photos, and
-- bulletin posts. Run against a demo database only (e.g. cputrade_demo),
-- AFTER the backend has booted once and AFTER seed-admin.sql.
--
-- Photos are real files under backend/uploads/products/demo-*.jpg (free
-- licences, credits in backend/uploads/products/CREDITS.txt) and are linked
-- here by URL, exactly the way the app stores uploaded photos.
--
-- Every account below logs in with password: Password123!
--   thabo_s, lerato_m, aisha_n, sizwe_d   STUDENT
--   campusprint                            VENDOR (approved)
--   quickfix_it                            VENDOR (NOT approved — admin approves it live in the demo)

USE cputrade_demo;

INSERT IGNORE INTO users (username, email, campus_handle, password_hash, role, verified, vendor_approved, account_status, created_at)
VALUES
  ('thabo_s',     'thabo_s@mycput.ac.za',      'thabo_s@cputmarket.ac.za',     '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'STUDENT', true, true,  'ACTIVE', NOW()),
  ('lerato_m',    'lerato_m@mycput.ac.za',     'lerato_m@cputmarket.ac.za',    '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'STUDENT', true, true,  'ACTIVE', NOW()),
  ('aisha_n',     'aisha_n@mycput.ac.za',      'aisha_n@cputmarket.ac.za',     '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'STUDENT', true, true,  'ACTIVE', NOW()),
  ('sizwe_d',     'sizwe_d@mycput.ac.za',      'sizwe_d@cputmarket.ac.za',     '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'STUDENT', true, true,  'ACTIVE', NOW()),
  ('campusprint', 'hello@campusprint.example', 'campusprint@cputmarket.ac.za', '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'VENDOR',  true, true,  'ACTIVE', NOW()),
  ('quickfix_it', 'support@quickfix.example',  'quickfix_it@cputmarket.ac.za', '$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW', 'VENDOR',  true, false, 'ACTIVE', NOW());

-- Staging table: one row per demo listing (and its photos).
DROP TEMPORARY TABLE IF EXISTS demo_listing;
CREATE TEMPORARY TABLE demo_listing (
  seller VARCHAR(30), title VARCHAR(150), description TEXT, price DECIMAL(10,2),
  category VARCHAR(30), item_condition VARCHAR(20), campus VARCHAR(30), quantity INT,
  open_to_swap BOOLEAN, swap_preferences VARCHAR(300), photo1 VARCHAR(80), photo2 VARCHAR(80), days_ago INT
);

INSERT INTO demo_listing VALUES
 ('thabo_s',  'Scientific calculator (2nd-F, 10-digit)', 'Does everything Maths 1 and Physics 1 need: trig, logs, stats. Batteries just replaced. Basic Citizen calculator included as a spare.', 180.00, 'ELECTRONICS', 'GOOD',     'BELLVILLE',    1, true,  'A Programming 1 textbook', 'demo-calculator.jpg', 'demo-calculator2.jpg', 1),
 ('thabo_s',  'Philosophy & physics textbook bundle (5 books)',   'Five classic philosophy and physics set books. Spines are worn but every page is intact. Sold as a bundle only.',                 450.00, 'TEXTBOOKS',   'GOOD',     'BELLVILLE',    1, false, NULL,                       'demo-textbooks.jpg', NULL, 2),
 ('thabo_s',  'Blue sports sneakers, size 9',            'Comfortable everyday trainers, worn for one semester. Cleaned and ready to go.',                                                         220.00, 'CLOTHING',    'GOOD',     'BELLVILLE',    1, false, NULL,                       'demo-sneakers.jpg', NULL, 3),
 ('lerato_m', 'Red Kanken-style backpack',               'Fits a 15" laptop plus books. Hardly used, no marks or tears. Perfect for the daily campus commute.',                                    380.00, 'CLOTHING',    'LIKE_NEW', 'DISTRICT_SIX', 1, false, NULL,                       'demo-backpack.jpg', NULL, 1),
 ('lerato_m', 'Free: shelf of old study books',          'Clearing my shelf. Mixed first-year study books and novels, free to anyone who will use them. Collect at District Six library.',          0.00, 'TEXTBOOKS',   'FAIR',     'DISTRICT_SIX', 1, false, NULL,                       'demo-bookshelf.jpg', NULL, 4),
 ('lerato_m', 'JBL wireless earbuds with charging case', 'True wireless earbuds, battery lasts a full day of lectures. Comes with the original charging case.',                                      500.00, 'ELECTRONICS', 'LIKE_NEW', 'DISTRICT_SIX', 1, false, NULL,                       'demo-earbuds.jpg', NULL, 12),
 ('aisha_n',  'Brass student microscope',                'Classic brass microscope, great for biology or as a desk piece. Lenses are clean and the focus works smoothly.',                       1200.00, 'OTHER',       'GOOD',     'GRANGER_BAY',  1, false, NULL,                       'demo-microscope.jpg', NULL, 2),
 ('aisha_n',  'Blue folding chairs (set of 2)',          'Two folding chairs with a matching bistro table, ideal for a res balcony. Sturdy and easy to carry.',                                   300.00, 'OTHER',       'GOOD',     'GRANGER_BAY',  1, false, NULL,                       'demo-chairs.jpg', NULL, 5),
 ('aisha_n',  'Acoustic guitar',                         'Warm-sounding acoustic guitar, new strings. Great for beginners. Open to a swap for a keyboard or ukulele.',                              750.00, 'OTHER',       'GOOD',     'MOWBRAY',      1, true,  'A keyboard or a ukulele',  'demo-guitar.jpg', NULL, 6),
 ('sizwe_d',  'White electric kettle (1.7L)',            'Fast-boil kettle, perfect for res rooms. Works perfectly, no limescale.',                                                                 90.00, 'OTHER',       'LIKE_NEW', 'WELLINGTON',   1, false, NULL,                       'demo-kettle.jpg', NULL, 1),
 ('sizwe_d',  'Glass kettle with stand',                 'Glass kettle with automatic switch-off. Used for one year.',                                                                             150.00, 'ELECTRONICS', 'GOOD',     'WELLINGTON',   1, false, NULL,                       'demo-kettle2.jpg', NULL, 7),
 ('sizwe_d',  'City bicycle',                            'Reliable commuter bike, gears and brakes recently serviced. Saves the taxi fare to campus.',                                           1500.00, 'OTHER',       'GOOD',     'MOWBRAY',      1, false, NULL,                       'demo-bicycle.jpg', NULL, 3),
 ('campusprint','Printing & document binding',           'Assignment printing and spiral binding, collected on campus. Price is per bound document. Same-day turnaround before 12:00.',            45.00, 'SERVICES',    'NEW',      'BELLVILLE',    50, false, NULL,                      'demo-documents.jpg', NULL, 2),
 ('campusprint','Maths & Physics tutoring (per hour)',   'One-on-one tutoring for first-year Maths and Physics. Past-paper practice included. Book a slot by sending a message.',                 120.00, 'SERVICES',    'NEW',      'MOWBRAY',      20, false, NULL,                      'demo-tutor.jpg', NULL, 4),
 ('campusprint','Drawing tablet with pen',               'Graphics tablet, ideal for design and architecture students. Includes pen and USB cable.',                                              650.00, 'ELECTRONICS', 'LIKE_NEW', 'BELLVILLE',    1, false, NULL,                       'demo-tablet.jpg', NULL, 5);

INSERT INTO products (seller_id, title, description, price, category, item_condition, campus,
                      quantity, open_to_swap, swap_preferences,
                      is_active, sold, buyer_confirmed, view_count, created_at, updated_at)
SELECT u.id, d.title, d.description, d.price, d.category, d.item_condition, d.campus,
       d.quantity, d.open_to_swap, d.swap_preferences,
       true, false, false, FLOOR(5 + RAND() * 80),
       NOW() - INTERVAL d.days_ago DAY, NOW() - INTERVAL d.days_ago DAY
FROM demo_listing d JOIN users u ON u.username = d.seller
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.seller_id = u.id AND p.title = d.title);

INSERT INTO product_images (product_id, sort_order, image_url)
SELECT p.id, 0, CONCAT('http://localhost:8080/uploads/products/', d.photo1)
FROM demo_listing d JOIN users u ON u.username = d.seller JOIN products p ON p.seller_id = u.id AND p.title = d.title
WHERE p.id NOT IN (SELECT DISTINCT product_id FROM product_images);

INSERT INTO product_images (product_id, sort_order, image_url)
SELECT p.id, 1, CONCAT('http://localhost:8080/uploads/products/', d.photo2)
FROM demo_listing d JOIN users u ON u.username = d.seller JOIN products p ON p.seller_id = u.id AND p.title = d.title
WHERE d.photo2 IS NOT NULL AND p.id NOT IN (SELECT product_id FROM (SELECT product_id FROM product_images WHERE sort_order = 1) x);

DROP TEMPORARY TABLE demo_listing;

-- --------------------------------------------------- bulletin board
INSERT INTO bulletin_posts (author_id, title, body, type, resolved, created_at)
SELECT u.id, s.title, s.body, s.type, false, NOW() - INTERVAL s.days_ago DAY
FROM (
  SELECT 'lerato_m' AS author, 'Lost: blue student card holder' AS title,
         'Lost near the District Six library on Monday. Please message me if found!' AS body, 'LOST' AS type, 1 AS days_ago
  UNION ALL SELECT 'thabo_s', 'Found: set of keys at Bellville cafeteria',
         'Three keys on a red lanyard. Handed in at the SRC office.', 'FOUND', 2
  UNION ALL SELECT 'thabo_s', 'Study group for Programming 1 exam',
         'Meeting in the IT building every Thursday at 14:00. All welcome, bring past papers.', 'GENERAL', 3
  UNION ALL SELECT 'aisha_n', 'Second-hand textbook swap day',
         'Bring your old textbooks to the Granger Bay student centre on Friday at 12:00 and swap them for the ones you need. No money needed.', 'GENERAL', 1
  UNION ALL SELECT 'campusprint', 'Exam-season printing special',
         'All printing 10% off during exam week. Show this post at the Bellville print shop.', 'GENERAL', 0
  UNION ALL SELECT 'sizwe_d', 'Found: black umbrella in the Mowbray lecture hall',
         'Left behind after the Tuesday lecture. I have it, message me to collect.', 'FOUND', 0
) AS s
JOIN users u ON u.username = s.author
WHERE NOT EXISTS (SELECT 1 FROM bulletin_posts b WHERE b.author_id = u.id AND b.title = s.title);
