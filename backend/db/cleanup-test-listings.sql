-- Remove test listings left in the local MySQL database by older integration
-- test runs. Those tests used synthetic users named flow_<32 hex characters>.
--
-- Back up the database before running. This deletes products and their
-- product-specific chats, reports, ratings, wishlists, and image references.
-- Listings with order history are deliberately preserved; admin audit
-- snapshots are preserved as well.

USE cputrade;

START TRANSACTION;

CREATE TEMPORARY TABLE cleanup_test_product_ids (
    product_id BIGINT PRIMARY KEY
) ENGINE=InnoDB;

INSERT INTO cleanup_test_product_ids (product_id)
SELECT p.id
FROM products p
JOIN users seller ON seller.id = p.seller_id
WHERE LEFT(seller.username, 5) = 'flow_'
  AND LENGTH(seller.username) = 37
  AND LOWER(SUBSTRING(seller.username, 6)) NOT REGEXP '[^0-9a-f]'
  AND NOT EXISTS (
      SELECT 1
      FROM marketplace_orders o
      WHERE o.product_id = p.id
  );

-- Review what will be removed before running this script.
SELECT p.id, p.title, seller.username, p.created_at
FROM products p
JOIN cleanup_test_product_ids candidate ON candidate.product_id = p.id
JOIN users seller ON seller.id = p.seller_id
ORDER BY p.id;

-- Keep listings with orders and their associated history intact.
SELECT p.id, p.title, seller.username, COUNT(o.id) AS order_count
FROM products p
JOIN users seller ON seller.id = p.seller_id
JOIN marketplace_orders o ON o.product_id = p.id
WHERE LEFT(seller.username, 5) = 'flow_'
  AND LENGTH(seller.username) = 37
  AND LOWER(SUBSTRING(seller.username, 6)) NOT REGEXP '[^0-9a-f]'
GROUP BY p.id, p.title, seller.username;

DELETE cm
FROM chat_messages cm
JOIN conversations c ON c.id = cm.conversation_id
JOIN cleanup_test_product_ids candidate ON candidate.product_id = c.product_id;

DELETE c
FROM conversations c
JOIN cleanup_test_product_ids candidate ON candidate.product_id = c.product_id;

DELETE wi
FROM wishlist_items wi
JOIN cleanup_test_product_ids candidate ON candidate.product_id = wi.product_id;

DELETE rt
FROM ratings rt
JOIN cleanup_test_product_ids candidate ON candidate.product_id = rt.product_id;

DELETE rp
FROM reports rp
JOIN cleanup_test_product_ids candidate ON candidate.product_id = rp.reported_product_id;

DELETE pi
FROM product_images pi
JOIN cleanup_test_product_ids candidate ON candidate.product_id = pi.product_id;

DELETE p
FROM products p
JOIN cleanup_test_product_ids candidate ON candidate.product_id = p.id;

COMMIT;

DROP TEMPORARY TABLE cleanup_test_product_ids;
