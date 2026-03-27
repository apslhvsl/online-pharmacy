-- ============================================================
-- SEED DATA FOR TESTING
-- auth_db, catalog_db, order_db
-- ============================================================

-- ============================================================
-- auth_db
-- ============================================================
\c auth_db;

-- Users: 1 admin + 3 customers
-- Passwords are BCrypt of "Test@123"
INSERT INTO users (name, email, mobile, password_hash, role, status, created_at, updated_at) VALUES
  ('Admin User',    'admin@pharmacy.com',   '9000000001', '$2a$10$uj0tbhftIGTmnnwGXMQXC./WeLI6f0nr3/ZGZcfhqRy4N.EBpNqF.', 'ADMIN',    'ACTIVE',    NOW(), NOW()),
  ('John Doe',      'john@example.com',     '9876543210', '$2a$10$uj0tbhftIGTmnnwGXMQXC./WeLI6f0nr3/ZGZcfhqRy4N.EBpNqF.', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Jane Smith',    'jane@example.com',     '9876543211', '$2a$10$uj0tbhftIGTmnnwGXMQXC./WeLI6f0nr3/ZGZcfhqRy4N.EBpNqF.', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Bob Suspended', 'bob@example.com',      '9876543212', '$2a$10$uj0tbhftIGTmnnwGXMQXC./WeLI6f0nr3/ZGZcfhqRy4N.EBpNqF.', 'CUSTOMER', 'SUSPENDED', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- catalog_db
-- ============================================================
\c catalog_db;

-- Categories
INSERT INTO categories (name, slug, icon_url, active, created_at) VALUES
  ('Pain Relief',            'pain-relief',          'https://cdn.example.com/icons/pain.png',        true, NOW()),
  ('Vitamins & Supplements', 'vitamins-supplements',  'https://cdn.example.com/icons/vitamins.png',    true, NOW()),
  ('Antibiotics',            'antibiotics',           'https://cdn.example.com/icons/antibiotics.png', true, NOW()),
  ('Diabetes Care',          'diabetes-care',         'https://cdn.example.com/icons/diabetes.png',    true, NOW()),
  ('Cold & Flu',             'cold-flu',              'https://cdn.example.com/icons/cold.png',        true, NOW())
ON CONFLICT (slug) DO NOTHING;

-- Medicines (no stock, no expiry_date — those live in inventory_batches)
INSERT INTO medicines (name, brand_name, active_ingredient, category_id, price, mrp, reorder_level, requires_prescription, dosage_form, strength, pack_size, description, manufacturer, is_featured, active, created_at, updated_at) VALUES
  -- Pain Relief (category 1)
  ('Paracetamol 500mg',  'Calpol',    'Paracetamol',     1, 25.00,  30.00,  20, false, 'Tablet',  '500mg',   '10 tablets',  'Pain reliever and fever reducer',          'GSK',        true,  true, NOW(), NOW()),
  ('Ibuprofen 400mg',    'Brufen',    'Ibuprofen',       1, 45.00,  55.00,  15, false, 'Tablet',  '400mg',   '10 tablets',  'Anti-inflammatory pain reliever',          'Abbott',     false, true, NOW(), NOW()),
  ('Aspirin 75mg',       'Ecosprin',  'Aspirin',         1, 18.00,  22.00,  10, false, 'Tablet',  '75mg',    '14 tablets',  'Low-dose aspirin for heart health',        'USV',        false, true, NOW(), NOW()),

  -- Vitamins (category 2)
  ('Vitamin C 500mg',    'Limcee',    'Ascorbic Acid',   2, 35.00,  40.00,  30, false, 'Tablet',  '500mg',   '15 tablets',  'Immunity booster vitamin C',               'Abbott',     true,  true, NOW(), NOW()),
  ('Vitamin D3 1000IU',  'D-Rise',    'Cholecalciferol', 2, 120.00, 140.00, 10, false, 'Capsule', '1000IU',  '30 capsules', 'Vitamin D3 supplement',                    'Sun Pharma', false, true, NOW(), NOW()),
  ('Multivitamin Daily', 'Supradyn',  'Multivitamin',    2, 180.00, 210.00, 10, false, 'Tablet',  'N/A',     '30 tablets',  'Complete daily multivitamin',              'Bayer',      true,  true, NOW(), NOW()),

  -- Antibiotics (category 3) — requires prescription
  ('Amoxicillin 500mg',  'Mox',       'Amoxicillin',     3, 85.00,  100.00, 15, true,  'Capsule', '500mg',   '10 capsules', 'Broad-spectrum antibiotic',                'Cipla',      false, true, NOW(), NOW()),
  ('Azithromycin 500mg', 'Azithral',  'Azithromycin',    3, 95.00,  115.00, 10, true,  'Tablet',  '500mg',   '3 tablets',   'Antibiotic for respiratory infections',    'Alembic',    false, true, NOW(), NOW()),

  -- Diabetes Care (category 4) — requires prescription
  ('Metformin 500mg',    'Glycomet',  'Metformin',       4, 30.00,  38.00,  20, true,  'Tablet',  '500mg',   '20 tablets',  'Oral diabetes medication',                 'USV',        false, true, NOW(), NOW()),
  ('Glimepiride 2mg',    'Amaryl',    'Glimepiride',     4, 65.00,  80.00,  10, true,  'Tablet',  '2mg',     '10 tablets',  'Sulfonylurea for type 2 diabetes',         'Sanofi',     false, true, NOW(), NOW()),

  -- Cold & Flu (category 5)
  ('Cetirizine 10mg',    'Cetzine',   'Cetirizine',      5, 22.00,  28.00,  20, false, 'Tablet',  '10mg',    '10 tablets',  'Antihistamine for allergy and cold',       'Cipla',      false, true, NOW(), NOW()),
  ('Dextromethorphan',   'Benadryl',  'DXM',             5, 55.00,  65.00,   5, false, 'Syrup',   '10mg/5ml','100ml',       'Cough suppressant syrup',                  'J&J',        false, true, NOW(), NOW()),

  -- Low stock test item
  ('Pantoprazole 40mg',  'Pan-D',     'Pantoprazole',    1, 48.00,  58.00,  20, false, 'Tablet',  '40mg',    '15 tablets',  'Proton pump inhibitor for acidity',        'Sun Pharma', false, true, NOW(), NOW()),

  -- Expiring-soon test item
  ('Ranitidine 150mg',   'Zinetac',   'Ranitidine',      1, 20.00,  25.00,  10, false, 'Tablet',  '150mg',   '10 tablets',  'H2 blocker for heartburn',                 'GSK',        false, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ============================================================
-- inventory_batches
-- Each medicine gets at least one batch. Quantities and expiry
-- dates are chosen to exercise low-stock and expiring-soon logic.
-- ============================================================
INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 25.00, 30.00, 200, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol 500mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 45.00, 55.00, 150, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen 400mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 18.00, 22.00, 80, '2026-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Aspirin 75mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 35.00, 40.00, 300, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C 500mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 120.00, 140.00, 90, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin D3 1000IU'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 180.00, 210.00, 60, '2027-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Multivitamin Daily'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 85.00, 100.00, 100, '2026-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Amoxicillin 500mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 95.00, 115.00, 70, '2026-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Azithromycin 500mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 30.00, 38.00, 120, '2027-04-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin 500mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 65.00, 80.00, 50, '2026-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Glimepiride 2mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 22.00, 28.00, 180, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Cetirizine 10mg'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 55.00, 65.00, 40, '2026-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Dextromethorphan'
ON CONFLICT DO NOTHING;

-- Low stock: quantity=5, reorder_level=20 → triggers low-stock alert
INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 48.00, 58.00, 5, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Pantoprazole 40mg'
ON CONFLICT DO NOTHING;

-- Expiring soon: expiry within 90 days of March 2026
INSERT INTO inventory_batches (medicine_id, batch_number, price, mrp, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BATCH-001', 20.00, 25.00, 30, '2026-05-01', NOW(), NOW() FROM medicines m WHERE m.name = 'Ranitidine 150mg'
ON CONFLICT DO NOTHING;

-- ============================================================
-- order_db
-- ============================================================
\c order_db;

-- Addresses for user 2 (john) and user 3 (jane)
INSERT INTO addresses (user_id, label, line1, line2, city, state, pincode, is_default, created_at) VALUES
  (2, 'Home',   '123 Main Street',   'Apt 4B',  'Mumbai',    'Maharashtra', '400001', true,  NOW()),
  (2, 'Office', '456 Business Park', 'Floor 3', 'Pune',      'Maharashtra', '411001', false, NOW()),
  (3, 'Home',   '789 Garden Road',   NULL,      'Bangalore', 'Karnataka',   '560001', true,  NOW())
ON CONFLICT DO NOTHING;

-- Carts
INSERT INTO carts (user_id, updated_at) VALUES
  (2, NOW()),
  (3, NOW())
ON CONFLICT (user_id) DO NOTHING;

-- Cart items for john (medicine_id 1 = Paracetamol, 4 = Vitamin C)
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity)
SELECT c.id, 1, 1, 'Paracetamol 500mg', 25.00, 2
FROM carts c WHERE c.user_id = 2
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity)
SELECT c.id, 4, 4, 'Vitamin C 500mg', 35.00, 1
FROM carts c WHERE c.user_id = 2
ON CONFLICT DO NOTHING;

-- Orders
INSERT INTO orders (order_number, user_id, address_id, status, subtotal, tax_amount, delivery_charge, total_amount, notes, created_at, updated_at) VALUES
  ('ORD-2026-0001', 2, 1, 'DELIVERED',       120.00, 6.00, 0.00, 126.00, NULL,                    NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days'),
  ('ORD-2026-0002', 2, 1, 'PAYMENT_PENDING', 180.00, 9.00, 0.00, 189.00, NULL,                    NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
  ('ORD-2026-0003', 3, 3, 'PAID',             85.00, 4.25, 0.00,  89.25, 'Requires prescription', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days')
ON CONFLICT (order_number) DO NOTHING;

-- Order items
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 1, 1, 'Paracetamol 500mg', 25.00, 2, 50.00 FROM orders o WHERE o.order_number = 'ORD-2026-0001'
ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 4, 4, 'Vitamin C 500mg', 35.00, 2, 70.00 FROM orders o WHERE o.order_number = 'ORD-2026-0001'
ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 6, 6, 'Multivitamin Daily', 180.00, 1, 180.00 FROM orders o WHERE o.order_number = 'ORD-2026-0002'
ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id, 7, 7, 'Amoxicillin 500mg', 85.00, 1, 85.00 FROM orders o WHERE o.order_number = 'ORD-2026-0003'
ON CONFLICT DO NOTHING;

-- Payments
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD', 'PAID', 126.00, 'TXN-COD-0001', NOW() - INTERVAL '9 days', NOW() - INTERVAL '10 days'
FROM orders o WHERE o.order_number = 'ORD-2026-0001'
ON CONFLICT (order_id) DO NOTHING;

INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT o.id, 'PREPAID', 'PENDING', 189.00, 'TXN-PRE-0002', NOW() - INTERVAL '1 day'
FROM orders o WHERE o.order_number = 'ORD-2026-0002'
ON CONFLICT (order_id) DO NOTHING;

INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD', 'PAID', 89.25, 'TXN-COD-0003', NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 days'
FROM orders o WHERE o.order_number = 'ORD-2026-0003'
ON CONFLICT (order_id) DO NOTHING;
