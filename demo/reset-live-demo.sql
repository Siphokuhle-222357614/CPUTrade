-- Puts the live-demo moments back to their starting state, so you can record again.
-- Run:  mysql -u root < demo\reset-live-demo.sql
-- Only touches: the Red Kanken sale and its rating, reports, appeals, and quickfix_it's approval.
-- The JBL earbuds sale (the "already sold and rated" example) is left alone.
-- Refresh every browser window afterwards.

USE cputrade;

SET @kanken := (SELECT id FROM products WHERE title LIKE 'Red Kanken%' LIMIT 1);

DELETE FROM ratings WHERE product_id = @kanken;

UPDATE products
SET sold = b'0', sold_at = NULL, sold_to_user_id = NULL,
    buyer_confirmed = b'0', buyer_confirmed_at = NULL, quantity = 1
WHERE id = @kanken;

DELETE FROM reports;
DELETE FROM appeals;

UPDATE users SET vendor_approved = b'0' WHERE username = 'quickfix_it';
UPDATE users SET account_status = 'ACTIVE', suspension_reason = NULL
WHERE username IN ('thabo_s', 'lerato_m', 'aisha_n', 'sizwe_d', 'campusprint', 'quickfix_it');
