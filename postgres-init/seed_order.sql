-- Clean slate before seeding (safe to re-run)
TRUNCATE TABLE payments, order_items, orders, cart_items, carts, addresses RESTART IDENTITY CASCADE;

-- Addresses
INSERT INTO addresses (user_id, label, line1, line2, city, state, pincode, is_default, created_at) VALUES
  (2, 'Home',   '123 Main Street',   'Apt 4B',  'Mumbai',    'Maharashtra', '400001', true,  NOW()),
  (2, 'Office', '456 Business Park', 'Floor 3', 'Pune',      'Maharashtra', '411001', false, NOW()),
  (3, 'Home',   '789 Garden Road',   NULL,      'Bangalore', 'Karnataka',   '560001', true,  NOW()),
  (4, 'Home',   '321 Lake View',     NULL,      'Chennai',   'Tamil Nadu',  '600001', true,  NOW()),
  (5, 'Home',   '654 Hill Street',   'Block B', 'Hyderabad', 'Telangana',   '500001', true,  NOW())
ON CONFLICT DO NOTHING;

-- Carts
INSERT INTO carts (user_id, updated_at) VALUES
  (2, NOW()), (3, NOW()), (4, NOW()), (5, NOW())
ON CONFLICT (user_id) DO NOTHING;

-- NOTE: batch_id and medicine_id values below match the insertion order in seed_catalog.sql
-- PCM-B001=1, VTC-B001=7, CET-B001=19 (1-indexed by insertion order)
-- medicine: Paracetamol=1, VitaminC=5, Cetirizine=15

-- Cart items for john (user 2)
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT c.id, 1, 1, 'Paracetamol 500mg', 25.00, 2, false FROM carts c WHERE c.user_id = 2
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT c.id, 7, 5, 'Vitamin C 500mg', 35.00, 1, false FROM carts c WHERE c.user_id = 2
ON CONFLICT DO NOTHING;

-- Cart items for alice (user 4)
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT c.id, 19, 15, 'Cetirizine 10mg', 22.00, 3, false FROM carts c WHERE c.user_id = 4
ON CONFLICT DO NOTHING;

-- Orders
INSERT INTO orders (order_number, user_id, address_id, status, subtotal, tax_amount, delivery_charge, total_amount, notes, created_at, updated_at) VALUES
  ('ORD-2026-0001', 2, 1, 'DELIVERED',          120.00,  6.00, 0.00, 126.00, NULL,                    NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days'),
  ('ORD-2026-0002', 2, 1, 'PAYMENT_PENDING',    180.00,  9.00, 0.00, 189.00, NULL,                    NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
  ('ORD-2026-0003', 3, 3, 'PAID',                85.00,  4.25, 0.00,  89.25, 'Requires prescription', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
  ('ORD-2026-0004', 2, 2, 'PACKED',              95.00,  4.75, 0.00,  99.75, NULL,                    NOW() - INTERVAL '3 days',  NOW() - INTERVAL '2 days'),
  ('ORD-2026-0005', 3, 3, 'OUT_FOR_DELIVERY',   215.00, 10.75, 0.00, 225.75, NULL,                    NOW() - INTERVAL '5 days',  NOW() - INTERVAL '1 day'),
  ('ORD-2026-0006', 4, 4, 'CUSTOMER_CANCELLED',  66.00,  3.30, 0.00,  69.30, 'Customer cancelled',    NOW() - INTERVAL '4 days',  NOW() - INTERVAL '3 days'),
  ('ORD-2026-0007', 5, 5, 'DELIVERED',           360.00, 18.00, 0.00, 378.00, NULL,                   NOW() - INTERVAL '15 days', NOW() - INTERVAL '8 days'),
  ('ORD-2026-0008', 4, 4, 'PAID',               110.00,  5.50, 0.00, 115.50, 'Requires prescription', NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day')
ON CONFLICT (order_number) DO NOTHING;

-- Order items (batch_id / medicine_id match catalog insertion order)
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 1,  1,  'Paracetamol 500mg',   25.00, 2,  50.00 FROM orders o WHERE o.order_number = 'ORD-2026-0001' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 7,  5,  'Vitamin C 500mg',     35.00, 2,  70.00 FROM orders o WHERE o.order_number = 'ORD-2026-0001' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 11, 7,  'Multivitamin Daily', 180.00, 1, 180.00 FROM orders o WHERE o.order_number = 'ORD-2026-0002' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 13, 9,  'Amoxicillin 500mg',   85.00, 1,  85.00 FROM orders o WHERE o.order_number = 'ORD-2026-0003' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 12, 8,  'Omega-3 1000mg',      95.00, 1,  95.00 FROM orders o WHERE o.order_number = 'ORD-2026-0004' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 16, 12, 'Metformin 500mg',     30.00, 2,  60.00 FROM orders o WHERE o.order_number = 'ORD-2026-0005' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 10, 6,  'Vitamin D3 1000IU', 120.00, 1, 120.00 FROM orders o WHERE o.order_number = 'ORD-2026-0005' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 19, 15, 'Cetirizine 10mg',     22.00, 3,  66.00 FROM orders o WHERE o.order_number = 'ORD-2026-0006' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 23, 18, 'Atorvastatin 10mg',   75.00, 2, 150.00 FROM orders o WHERE o.order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 24, 19, 'Amlodipine 5mg',      45.00, 2,  90.00 FROM orders o WHERE o.order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 15, 11, 'Ciprofloxacin 500mg',110.00, 1, 110.00 FROM orders o WHERE o.order_number = 'ORD-2026-0008' ON CONFLICT DO NOTHING;

-- Payments
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',     126.00, 'TXN-COD-0001', NOW() - INTERVAL '9 days',   NOW() - INTERVAL '10 days' FROM orders o WHERE o.order_number = 'ORD-2026-0001' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT o.id, 'PREPAID', 'PENDING',  189.00, 'TXN-PRE-0002', NOW() - INTERVAL '1 day'    FROM orders o WHERE o.order_number = 'ORD-2026-0002' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',      89.25, 'TXN-COD-0003', NOW() - INTERVAL '1 day',   NOW() - INTERVAL '2 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0003' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'PREPAID', 'PAID',      99.75, 'TXN-PRE-0004', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '3 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0004' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',     225.75, 'TXN-COD-0005', NOW() - INTERVAL '4 days',  NOW() - INTERVAL '5 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0005' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT o.id, 'PREPAID', 'REFUNDED',  69.30, 'TXN-PRE-0006', NOW() - INTERVAL '4 days'   FROM orders o WHERE o.order_number = 'ORD-2026-0006' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',     378.00, 'TXN-COD-0007', NOW() - INTERVAL '13 days', NOW() - INTERVAL '15 days' FROM orders o WHERE o.order_number = 'ORD-2026-0007' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'PREPAID', 'PAID',     115.50, 'TXN-PRE-0008', NOW() - INTERVAL '12 hours',NOW() - INTERVAL '1 day'   FROM orders o WHERE o.order_number = 'ORD-2026-0008' ON CONFLICT (order_id) DO NOTHING;
