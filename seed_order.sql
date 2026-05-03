-- ============================================================
-- Order seed — medicine/batch IDs sourced from catalog_db
-- medicine_id / batch_id reference catalog_db values directly
--   Paracetamol       medicine_id=1,  batch_id=1  (PCM-B001)
--   Vitamin C         medicine_id=46, batch_id=52 (VTC-B001)
--   Multivitamin      medicine_id=47, batch_id=54 (MVT-B001)
--   Azithromycin      medicine_id=8,  batch_id=11 (AZI-B001)
--   Cefixime          medicine_id=9,  batch_id=12 (CEF-B001)
--   Metformin         medicine_id=36, batch_id=41 (MET-B001)
--   Vitamin D3        medicine_id=48, batch_id=55 (VTD-B001)
--   Cetirizine        medicine_id=22, batch_id=26 (CET-B001)
--   Atorvastatin      medicine_id=40, batch_id=46 (ATO-B001)
--   Amlodipine        medicine_id=38, batch_id=44 (AML-B001)
--   Telmisartan       medicine_id=39, batch_id=45 (TEL-B001)
--   Levofloxacin      medicine_id=11, batch_id=14 (LEV-B001)
-- ============================================================

TRUNCATE TABLE payments, order_items, orders, cart_items, carts, addresses RESTART IDENTITY CASCADE;

-- ── Addresses ────────────────────────────────────────────────
INSERT INTO addresses (user_id, label, line1, line2, city, state, pincode, is_default, created_at) VALUES
  (2, 'Home',   '123 Main Street',   'Apt 4B',  'Mumbai',    'Maharashtra', '400001', true,  NOW()),
  (2, 'Office', '456 Business Park', 'Floor 3', 'Pune',      'Maharashtra', '411001', false, NOW()),
  (3, 'Home',   '789 Garden Road',   NULL,      'Bangalore', 'Karnataka',   '560001', true,  NOW()),
  (4, 'Home',   '321 Lake View',     NULL,      'Chennai',   'Tamil Nadu',  '600001', true,  NOW()),
  (5, 'Home',   '654 Hill Street',   'Block B', 'Hyderabad', 'Telangana',   '500001', true,  NOW())
ON CONFLICT DO NOTHING;

-- ── Carts ────────────────────────────────────────────────────
INSERT INTO carts (user_id, updated_at) VALUES
  (2, NOW()), (3, NOW()), (4, NOW()), (5, NOW())
ON CONFLICT (user_id) DO NOTHING;

-- ── Cart items ───────────────────────────────────────────────
-- john (user 2): Paracetamol + Vitamin C
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT id,  1,  1, 'Paracetamol', 25.00,  2, false FROM carts WHERE user_id = 2 ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT id, 52, 46, 'Vitamin C',  120.00,  1, false FROM carts WHERE user_id = 2 ON CONFLICT DO NOTHING;

-- alice (user 4): Cetirizine
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT id, 26, 22, 'Cetirizine',  30.00,  3, false FROM carts WHERE user_id = 4 ON CONFLICT DO NOTHING;

-- ── Orders ───────────────────────────────────────────────────
INSERT INTO orders (order_number, user_id, address_id, status, subtotal, tax_amount, delivery_charge, total_amount, notes, created_at, updated_at) VALUES
  ('ORD-2026-0001', 2, 1, 'DELIVERED',          170.00,  8.50, 0.00, 178.50, NULL,                    NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days'),
  ('ORD-2026-0002', 2, 1, 'PAYMENT_PENDING',    150.00,  7.50, 0.00, 157.50, NULL,                    NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
  ('ORD-2026-0003', 3, 3, 'PAID',               120.00,  6.00, 0.00, 126.00, 'Requires prescription', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
  ('ORD-2026-0004', 2, 2, 'PACKED',             140.00,  7.00, 0.00, 147.00, NULL,                    NOW() - INTERVAL '3 days',  NOW() - INTERVAL '2 days'),
  ('ORD-2026-0005', 3, 3, 'OUT_FOR_DELIVERY',   190.00,  9.50, 0.00, 199.50, NULL,                    NOW() - INTERVAL '5 days',  NOW() - INTERVAL '1 day'),
  ('ORD-2026-0006', 4, 4, 'CUSTOMER_CANCELLED',  90.00,  4.50, 0.00,  94.50, 'Customer cancelled',    NOW() - INTERVAL '4 days',  NOW() - INTERVAL '3 days'),
  ('ORD-2026-0007', 5, 5, 'DELIVERED',          340.00, 17.00, 0.00, 357.00, NULL,                    NOW() - INTERVAL '15 days', NOW() - INTERVAL '8 days'),
  ('ORD-2026-0008', 4, 4, 'PAID',               150.00,  7.50, 0.00, 157.50, 'Requires prescription', NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day')
ON CONFLICT (order_number) DO NOTHING;

-- ── Order items ──────────────────────────────────────────────
-- ORD-2026-0001: Paracetamol x2 + Vitamin C x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id,  1,  1, 'Paracetamol',  25.00, 2,  50.00 FROM orders WHERE order_number = 'ORD-2026-0001' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 52, 46, 'Vitamin C',   120.00, 1, 120.00 FROM orders WHERE order_number = 'ORD-2026-0001' ON CONFLICT DO NOTHING;

-- ORD-2026-0002: Multivitamin Tablets x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 54, 47, 'Multivitamin Tablets', 150.00, 1, 150.00 FROM orders WHERE order_number = 'ORD-2026-0002' ON CONFLICT DO NOTHING;

-- ORD-2026-0003: Azithromycin x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 11,  8, 'Azithromycin', 120.00, 1, 120.00 FROM orders WHERE order_number = 'ORD-2026-0003' ON CONFLICT DO NOTHING;

-- ORD-2026-0004: Cefixime x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 12,  9, 'Cefixime', 140.00, 1, 140.00 FROM orders WHERE order_number = 'ORD-2026-0004' ON CONFLICT DO NOTHING;

-- ORD-2026-0005: Metformin x2 + Vitamin D3 x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 41, 36, 'Metformin',   55.00, 2, 110.00 FROM orders WHERE order_number = 'ORD-2026-0005' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 55, 48, 'Vitamin D3',  80.00, 1,  80.00 FROM orders WHERE order_number = 'ORD-2026-0005' ON CONFLICT DO NOTHING;

-- ORD-2026-0006: Cetirizine x3
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 26, 22, 'Cetirizine', 30.00, 3, 90.00 FROM orders WHERE order_number = 'ORD-2026-0006' ON CONFLICT DO NOTHING;

-- ORD-2026-0007: Atorvastatin x2 + Amlodipine x2 + Telmisartan x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 46, 40, 'Atorvastatin',  90.00, 2, 180.00 FROM orders WHERE order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 44, 38, 'Amlodipine',    45.00, 2,  90.00 FROM orders WHERE order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 45, 39, 'Telmisartan',  110.00, 1, 110.00 FROM orders WHERE order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;

-- ORD-2026-0008: Levofloxacin x1
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT id, 14, 11, 'Levofloxacin', 150.00, 1, 150.00 FROM orders WHERE order_number = 'ORD-2026-0008' ON CONFLICT DO NOTHING;

-- ── Payments ─────────────────────────────────────────────────
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT id, 'COD',     'PAID',     178.50, 'TXN-COD-0001', NOW() - INTERVAL '9 days',    NOW() - INTERVAL '10 days' FROM orders WHERE order_number = 'ORD-2026-0001' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT id, 'PREPAID', 'PENDING',  157.50, 'TXN-PRE-0002', NOW() - INTERVAL '1 day'      FROM orders WHERE order_number = 'ORD-2026-0002' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT id, 'COD',     'PAID',     126.00, 'TXN-COD-0003', NOW() - INTERVAL '1 day',     NOW() - INTERVAL '2 days'  FROM orders WHERE order_number = 'ORD-2026-0003' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT id, 'PREPAID', 'PAID',     147.00, 'TXN-PRE-0004', NOW() - INTERVAL '2 days',    NOW() - INTERVAL '3 days'  FROM orders WHERE order_number = 'ORD-2026-0004' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT id, 'COD',     'PAID',     199.50, 'TXN-COD-0005', NOW() - INTERVAL '4 days',    NOW() - INTERVAL '5 days'  FROM orders WHERE order_number = 'ORD-2026-0005' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT id, 'PREPAID', 'REFUNDED',  94.50, 'TXN-PRE-0006', NOW() - INTERVAL '4 days'     FROM orders WHERE order_number = 'ORD-2026-0006' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT id, 'COD',     'PAID',     357.00, 'TXN-COD-0007', NOW() - INTERVAL '13 days',   NOW() - INTERVAL '15 days' FROM orders WHERE order_number = 'ORD-2026-0007' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT id, 'PREPAID', 'PAID',     157.50, 'TXN-PRE-0008', NOW() - INTERVAL '12 hours',  NOW() - INTERVAL '1 day'   FROM orders WHERE order_number = 'ORD-2026-0008' ON CONFLICT (order_id) DO NOTHING;
