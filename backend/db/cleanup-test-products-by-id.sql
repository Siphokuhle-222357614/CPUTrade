-- Clean up the test products from the failed Workbench delete attempt.
--
-- Back up cputrade first. Run this script once with @confirm_test_product_cleanup = 0
-- and review the preview output. If every listed product is test data, change
-- the value below to 1 and run the script again.
--
-- Products with marketplace_orders are retained (and hidden from the public
-- marketplace) so order history and its foreign keys remain intact. Products
-- without orders are deleted along with their product-specific dependent rows.
-- Seller accounts and admin action snapshots are not deleted.

USE cputrade;

SET @confirm_test_product_cleanup := 0;

START TRANSACTION;

CREATE TEMPORARY TABLE cleanup_target_product_ids (
    product_id BIGINT PRIMARY KEY
) ENGINE=InnoDB;

INSERT IGNORE INTO cleanup_target_product_ids (product_id)
SELECT id
FROM products
WHERE id IN (19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30);

-- Preview: verify titles and sellers before changing the confirmation value.
SELECT
    p.id,
    p.title,
    seller.username AS seller,
    COUNT(o.id) AS order_count,
    CASE WHEN COUNT(o.id) > 0 THEN 'will be hidden; order history preserved'
         ELSE 'will be deleted with test-only dependent rows'
    END AS cleanup_action
FROM products p
JOIN cleanup_target_product_ids target ON target.product_id = p.id
JOIN users seller ON seller.id = p.seller_id
LEFT JOIN marketplace_orders o ON o.product_id = p.id
GROUP BY p.id, p.title, seller.username
ORDER BY p.id;

-- Keep products with order history, but hide them from public marketplace results.
UPDATE products p
JOIN cleanup_target_product_ids target ON target.product_id = p.id
SET p.is_active = false
WHERE @confirm_test_product_cleanup = 1
  AND EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = p.id
  );

-- Delete dependent data before products to satisfy MySQL foreign keys.
DELETE cm
FROM chat_messages cm
JOIN conversations c ON c.id = cm.conversation_id
JOIN cleanup_target_product_ids target ON target.product_id = c.product_id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = c.product_id
  );

DELETE c
FROM conversations c
JOIN cleanup_target_product_ids target ON target.product_id = c.product_id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = c.product_id
  );

DELETE wi
FROM wishlist_items wi
JOIN cleanup_target_product_ids target ON target.product_id = wi.product_id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = wi.product_id
  );

DELETE rt
FROM ratings rt
JOIN cleanup_target_product_ids target ON target.product_id = rt.product_id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = rt.product_id
  );

DELETE rp
FROM reports rp
JOIN cleanup_target_product_ids target ON target.product_id = rp.reported_product_id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = rp.reported_product_id
  );

DELETE pi
FROM product_images pi
JOIN cleanup_target_product_ids target ON target.product_id = pi.product_id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = pi.product_id
  );

DELETE p
FROM products p
JOIN cleanup_target_product_ids target ON target.product_id = p.id
WHERE @confirm_test_product_cleanup = 1
  AND NOT EXISTS (
      SELECT 1 FROM marketplace_orders o WHERE o.product_id = p.id
  );

COMMIT;

-- After a confirmed run, any remaining target product has protected order history.
SELECT
    p.id,
    p.title,
    seller.username AS seller,
    COUNT(o.id) AS order_count,
    p.is_active
FROM products p
JOIN cleanup_target_product_ids target ON target.product_id = p.id
JOIN users seller ON seller.id = p.seller_id
LEFT JOIN marketplace_orders o ON o.product_id = p.id
GROUP BY p.id, p.title, seller.username, p.is_active
ORDER BY p.id;

DROP TEMPORARY TABLE cleanup_target_product_ids;
