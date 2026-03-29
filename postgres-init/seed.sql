-- ============================================================
-- SEED DATA FOR TESTING
-- auth_db, catalog_db, order_db
-- ============================================================

-- ============================================================
-- auth_db
-- ============================================================
\c auth_db;

-- Passwords are BCrypt of "Test@123"
INSERT INTO users (name, email, mobile, password_hash, role, status, created_at, updated_at) VALUES
  ('Admin User',    'admin@pharmacy.com', '9000000001', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'ADMIN',    'ACTIVE',    NOW(), NOW()),
  ('John Doe',      'john@example.com',   '9876543210', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Jane Smith',    'jane@example.com',   '9876543211', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Bob Suspended', 'bob@example.com',    '9876543212', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'SUSPENDED', NOW(), NOW()),
  ('Alice Brown',   'alice@example.com',  '9876543213', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Charlie Dev',   'charlie@example.com','9876543214', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- catalog_db
-- ============================================================
\c catalog_db;

-- Categories (only columns that exist on the entity)
INSERT INTO categories (name, slug, active, created_at) VALUES
  ('Pain Relief',            'pain-relief',         true, NOW()),
  ('Vitamins & Supplements', 'vitamins-supplements', true, NOW()),
  ('Antibiotics',            'antibiotics',          true, NOW()),
  ('Diabetes Care',          'diabetes-care',        true, NOW()),
  ('Cold & Flu',             'cold-flu',             true, NOW()),
  ('Cardiac Care',           'cardiac-care',         true, NOW()),
  ('Gastrointestinal',       'gastrointestinal',     true, NOW())
ON CONFLICT (slug) DO NOTHING;


-- Medicines — columns: name, category_id, price, active, requires_prescription,
--             manufacturer, strength, pack_size, description, image_url, reorder_level
INSERT INTO medicines (name, category_id, price, active, requires_prescription, manufacturer, strength, pack_size, description, reorder_level, created_at, updated_at) VALUES
  -- Pain Relief (cat 1)
  ('Paracetamol 500mg',   1,  25.00, true, false, 'GSK',        '500mg',    '10 tablets',  'Pain reliever and fever reducer',           20, NOW(), NOW()),
  ('Ibuprofen 400mg',     1,  45.00, true, false, 'Abbott',     '400mg',    '10 tablets',  'Anti-inflammatory pain reliever',           15, NOW(), NOW()),
  ('Aspirin 75mg',        1,  18.00, true, false, 'USV',        '75mg',     '14 tablets',  'Low-dose aspirin for heart health',         10, NOW(), NOW()),
  ('Diclofenac 50mg',     1,  38.00, true, false, 'Novartis',   '50mg',     '10 tablets',  'NSAID for pain and inflammation',           10, NOW(), NOW()),

  -- Vitamins (cat 2)
  ('Vitamin C 500mg',     2,  35.00, true, false, 'Abbott',     '500mg',    '15 tablets',  'Immunity booster vitamin C',                30, NOW(), NOW()),
  ('Vitamin D3 1000IU',   2, 120.00, true, false, 'Sun Pharma', '1000IU',   '30 capsules', 'Vitamin D3 supplement',                     10, NOW(), NOW()),
  ('Multivitamin Daily',  2, 180.00, true, false, 'Bayer',      'N/A',      '30 tablets',  'Complete daily multivitamin',               10, NOW(), NOW()),
  ('Omega-3 1000mg',      2,  95.00, true, false, 'Himalaya',   '1000mg',   '30 capsules', 'Fish oil omega-3 supplement',               10, NOW(), NOW()),

  -- Antibiotics (cat 3) — prescription required
  ('Amoxicillin 500mg',   3,  85.00, true, true,  'Cipla',      '500mg',    '10 capsules', 'Broad-spectrum antibiotic',                 15, NOW(), NOW()),
  ('Azithromycin 500mg',  3,  95.00, true, true,  'Alembic',    '500mg',    '3 tablets',   'Antibiotic for respiratory infections',     10, NOW(), NOW()),
  ('Ciprofloxacin 500mg', 3, 110.00, true, true,  'Cipla',      '500mg',    '10 tablets',  'Fluoroquinolone antibiotic',                10, NOW(), NOW()),

  -- Diabetes Care (cat 4) — prescription required
  ('Metformin 500mg',     4,  30.00, true, true,  'USV',        '500mg',    '20 tablets',  'Oral diabetes medication',                  20, NOW(), NOW()),
  ('Glimepiride 2mg',     4,  65.00, true, true,  'Sanofi',     '2mg',      '10 tablets',  'Sulfonylurea for type 2 diabetes',          10, NOW(), NOW()),
  ('Insulin Glargine',    4, 850.00, true, true,  'Sanofi',     '100IU/ml', '1 vial',      'Long-acting insulin for diabetes',           5, NOW(), NOW()),

  -- Cold & Flu (cat 5)
  ('Cetirizine 10mg',     5,  22.00, true, false, 'Cipla',      '10mg',     '10 tablets',  'Antihistamine for allergy and cold',        20, NOW(), NOW()),
  ('Dextromethorphan',    5,  55.00, true, false, 'J&J',        '10mg/5ml', '100ml',       'Cough suppressant syrup',                    5, NOW(), NOW()),
  ('Phenylephrine 10mg',  5,  30.00, true, false, 'GSK',        '10mg',     '10 tablets',  'Nasal decongestant',                        10, NOW(), NOW()),

  -- Cardiac Care (cat 6) — prescription required
  ('Atorvastatin 10mg',   6,  75.00, true, true,  'Pfizer',     '10mg',     '10 tablets',  'Statin for cholesterol management',         15, NOW(), NOW()),
  ('Amlodipine 5mg',      6,  45.00, true, true,  'Pfizer',     '5mg',      '10 tablets',  'Calcium channel blocker for hypertension',  15, NOW(), NOW()),

  -- Gastrointestinal (cat 7)
  ('Pantoprazole 40mg',   7,  48.00, true, false, 'Sun Pharma', '40mg',     '15 tablets',  'Proton pump inhibitor for acidity',         20, NOW(), NOW()),
  ('Ranitidine 150mg',    7,  20.00, true, false, 'GSK',        '150mg',    '10 tablets',  'H2 blocker for heartburn',                  10, NOW(), NOW()),
  ('Domperidone 10mg',    7,  28.00, true, false, 'Cipla',      '10mg',     '10 tablets',  'Anti-nausea and motility agent',            10, NOW(), NOW()),

  -- Inactive medicine (to test active=false filtering)
  ('Discontinued Drug',   1,  10.00, false, false, 'Unknown',   '10mg',     '10 tablets',  'Discontinued — should not appear in catalog', 0, NOW(), NOW())
ON CONFLICT DO NOTHING;


-- ============================================================
-- inventory_batches — columns: medicine_id, batch_number, price, quantity, expiry_date
-- Multiple batches per medicine to test FEFO deduction.
-- Special cases: low-stock, expiring-soon, already-expired.
-- ============================================================

-- Paracetamol: 2 batches (FEFO test — older expiry deducted first)
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PCM-B001', 25.00, 100, '2026-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PCM-B002', 26.00, 150, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol 500mg';

-- Ibuprofen: 2 batches
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IBU-B001', 45.00, 80, '2026-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen 400mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IBU-B002', 44.00, 120, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen 400mg';

-- Aspirin: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ASP-B001', 18.00, 80, '2026-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Aspirin 75mg';

-- Diclofenac: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DIC-B001', 38.00, 60, '2027-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Diclofenac 50mg';

-- Vitamin C: 2 batches
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTC-B001', 35.00, 200, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTC-B002', 34.00, 100, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C 500mg';

-- Vitamin D3: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTD-B001', 120.00, 90, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin D3 1000IU';

-- Multivitamin: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MUL-B001', 180.00, 60, '2027-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Multivitamin Daily';

-- Omega-3: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'OMG-B001', 95.00, 45, '2027-04-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Omega-3 1000mg';

-- Amoxicillin: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AMX-B001', 85.00, 100, '2026-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Amoxicillin 500mg';

-- Azithromycin: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AZI-B001', 95.00, 70, '2026-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Azithromycin 500mg';

-- Ciprofloxacin: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CIP-B001', 110.00, 50, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Ciprofloxacin 500mg';

-- Metformin: 2 batches
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MET-B001', 30.00, 120, '2027-04-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MET-B002', 29.00, 80, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin 500mg';

-- Glimepiride: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'GLI-B001', 65.00, 50, '2026-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Glimepiride 2mg';

-- Insulin Glargine: 1 batch (low stock — qty=3, reorder_level=5)
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'INS-B001', 850.00, 3, '2026-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Insulin Glargine';

-- Cetirizine: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CET-B001', 22.00, 180, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Cetirizine 10mg';

-- Dextromethorphan: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DEX-B001', 55.00, 40, '2026-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Dextromethorphan';

-- Phenylephrine: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PHE-B001', 30.00, 75, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Phenylephrine 10mg';

-- Atorvastatin: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ATO-B001', 75.00, 90, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Atorvastatin 10mg';

-- Amlodipine: 1 batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AML-B001', 45.00, 110, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Amlodipine 5mg';

-- Pantoprazole: LOW STOCK — qty=5, reorder_level=20
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PAN-B001', 48.00, 5, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Pantoprazole 40mg';

-- Ranitidine: EXPIRING SOON — within 60 days
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'RAN-B001', 20.00, 30, '2026-05-15', NOW(), NOW() FROM medicines m WHERE m.name = 'Ranitidine 150mg';

-- Domperidone: 1 batch + 1 ALREADY EXPIRED batch (to test expiry filtering)
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DOM-B001', 28.00, 60, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Domperidone 10mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DOM-B000', 25.00, 10, '2025-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Domperidone 10mg';


-- ============================================================
-- order_db
-- ============================================================
\c order_db;

-- Addresses
INSERT INTO addresses (user_id, label, line1, line2, city, state, pincode, is_default, created_at) VALUES
  (2, 'Home',   '123 Main Street',    'Apt 4B',   'Mumbai',    'Maharashtra', '400001', true,  NOW()),
  (2, 'Office', '456 Business Park',  'Floor 3',  'Pune',      'Maharashtra', '411001', false, NOW()),
  (3, 'Home',   '789 Garden Road',    NULL,       'Bangalore', 'Karnataka',   '560001', true,  NOW()),
  (4, 'Home',   '321 Lake View',      NULL,       'Chennai',   'Tamil Nadu',  '600001', true,  NOW()),  -- alice
  (5, 'Home',   '654 Hill Street',    'Block B',  'Hyderabad', 'Telangana',   '500001', true,  NOW())   -- charlie
ON CONFLICT DO NOTHING;

-- Carts (one per active customer)
INSERT INTO carts (user_id, updated_at) VALUES
  (2, NOW()),
  (3, NOW()),
  (4, NOW()),
  (5, NOW())
ON CONFLICT (user_id) DO NOTHING;

-- Cart items for john (user 2) — uses subquery for batch_id to avoid hardcoding
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT c.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Paracetamol 500mg' AND b.batch_number = 'PCM-B001'),
       (SELECT id FROM medicines WHERE name = 'Paracetamol 500mg'),
       'Paracetamol 500mg', 25.00, 2, false
FROM carts c WHERE c.user_id = 2
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT c.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Vitamin C 500mg' AND b.batch_number = 'VTC-B001'),
       (SELECT id FROM medicines WHERE name = 'Vitamin C 500mg'),
       'Vitamin C 500mg', 35.00, 1, false
FROM carts c WHERE c.user_id = 2
ON CONFLICT DO NOTHING;

-- Cart items for alice (user 4)
INSERT INTO cart_items (cart_id, batch_id, medicine_id, medicine_name, unit_price, quantity, requires_prescription)
SELECT c.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Cetirizine 10mg' AND b.batch_number = 'CET-B001'),
       (SELECT id FROM medicines WHERE name = 'Cetirizine 10mg'),
       'Cetirizine 10mg', 22.00, 3, false
FROM carts c WHERE c.user_id = 4
ON CONFLICT DO NOTHING;

-- Orders covering all statuses
INSERT INTO orders (order_number, user_id, address_id, status, subtotal, tax_amount, delivery_charge, total_amount, notes, created_at, updated_at) VALUES
  ('ORD-2026-0001', 2, 1, 'DELIVERED',        120.00,  6.00, 0.00, 126.00,  NULL,                     NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days'),
  ('ORD-2026-0002', 2, 1, 'PAYMENT_PENDING',  180.00,  9.00, 0.00, 189.00,  NULL,                     NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
  ('ORD-2026-0003', 3, 3, 'PAID',              85.00,  4.25, 0.00,  89.25,  'Requires prescription',  NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
  ('ORD-2026-0004', 2, 2, 'PROCESSING',        95.00,  4.75, 0.00,  99.75,  NULL,                     NOW() - INTERVAL '3 days',  NOW() - INTERVAL '2 days'),
  ('ORD-2026-0005', 3, 3, 'SHIPPED',          215.00, 10.75, 0.00, 225.75,  NULL,                     NOW() - INTERVAL '5 days',  NOW() - INTERVAL '1 day'),
  ('ORD-2026-0006', 4, 4, 'CANCELLED',         66.00,  3.30, 0.00,  69.30,  'Customer cancelled',     NOW() - INTERVAL '4 days',  NOW() - INTERVAL '3 days'),
  ('ORD-2026-0007', 5, 5, 'DELIVERED',        360.00, 18.00, 0.00, 378.00,  NULL,                     NOW() - INTERVAL '15 days', NOW() - INTERVAL '8 days'),
  ('ORD-2026-0008', 4, 4, 'PAID',             110.00,  5.50, 0.00, 115.50,  'Requires prescription',  NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day')
ON CONFLICT (order_number) DO NOTHING;

-- Order items
INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Paracetamol 500mg' AND b.batch_number = 'PCM-B001'),
       (SELECT id FROM medicines WHERE name = 'Paracetamol 500mg'),
       'Paracetamol 500mg', 25.00, 2, 50.00
FROM orders o WHERE o.order_number = 'ORD-2026-0001' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Vitamin C 500mg' AND b.batch_number = 'VTC-B001'),
       (SELECT id FROM medicines WHERE name = 'Vitamin C 500mg'),
       'Vitamin C 500mg', 35.00, 2, 70.00
FROM orders o WHERE o.order_number = 'ORD-2026-0001' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Multivitamin Daily' AND b.batch_number = 'MUL-B001'),
       (SELECT id FROM medicines WHERE name = 'Multivitamin Daily'),
       'Multivitamin Daily', 180.00, 1, 180.00
FROM orders o WHERE o.order_number = 'ORD-2026-0002' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Amoxicillin 500mg' AND b.batch_number = 'AMX-B001'),
       (SELECT id FROM medicines WHERE name = 'Amoxicillin 500mg'),
       'Amoxicillin 500mg', 85.00, 1, 85.00
FROM orders o WHERE o.order_number = 'ORD-2026-0003' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Omega-3 1000mg' AND b.batch_number = 'OMG-B001'),
       (SELECT id FROM medicines WHERE name = 'Omega-3 1000mg'),
       'Omega-3 1000mg', 95.00, 1, 95.00
FROM orders o WHERE o.order_number = 'ORD-2026-0004' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Metformin 500mg' AND b.batch_number = 'MET-B001'),
       (SELECT id FROM medicines WHERE name = 'Metformin 500mg'),
       'Metformin 500mg', 30.00, 2, 60.00
FROM orders o WHERE o.order_number = 'ORD-2026-0005' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Vitamin D3 1000IU' AND b.batch_number = 'VTD-B001'),
       (SELECT id FROM medicines WHERE name = 'Vitamin D3 1000IU'),
       'Vitamin D3 1000IU', 120.00, 1, 120.00
FROM orders o WHERE o.order_number = 'ORD-2026-0005' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Cetirizine 10mg' AND b.batch_number = 'CET-B001'),
       (SELECT id FROM medicines WHERE name = 'Cetirizine 10mg'),
       'Cetirizine 10mg', 22.00, 3, 66.00
FROM orders o WHERE o.order_number = 'ORD-2026-0006' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Atorvastatin 10mg' AND b.batch_number = 'ATO-B001'),
       (SELECT id FROM medicines WHERE name = 'Atorvastatin 10mg'),
       'Atorvastatin 10mg', 75.00, 2, 150.00
FROM orders o WHERE o.order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Amlodipine 5mg' AND b.batch_number = 'AML-B001'),
       (SELECT id FROM medicines WHERE name = 'Amlodipine 5mg'),
       'Amlodipine 5mg', 45.00, 2, 90.00
FROM orders o WHERE o.order_number = 'ORD-2026-0007' ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, batch_id, medicine_id, medicine_name, unit_price, quantity, line_total)
SELECT o.id,
       (SELECT b.id FROM inventory_batches b JOIN medicines m ON b.medicine_id = m.id WHERE m.name = 'Ciprofloxacin 500mg' AND b.batch_number = 'CIP-B001'),
       (SELECT id FROM medicines WHERE name = 'Ciprofloxacin 500mg'),
       'Ciprofloxacin 500mg', 110.00, 1, 110.00
FROM orders o WHERE o.order_number = 'ORD-2026-0008' ON CONFLICT DO NOTHING;

-- Payments
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',    126.00, 'TXN-COD-0001', NOW() - INTERVAL '9 days',  NOW() - INTERVAL '10 days' FROM orders o WHERE o.order_number = 'ORD-2026-0001' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT o.id, 'PREPAID', 'PENDING', 189.00, 'TXN-PRE-0002', NOW() - INTERVAL '1 day'   FROM orders o WHERE o.order_number = 'ORD-2026-0002' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',     89.25, 'TXN-COD-0003', NOW() - INTERVAL '1 day',  NOW() - INTERVAL '2 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0003' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'PREPAID', 'PAID',     99.75, 'TXN-PRE-0004', NOW() - INTERVAL '2 days', NOW() - INTERVAL '3 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0004' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',    225.75, 'TXN-COD-0005', NOW() - INTERVAL '4 days', NOW() - INTERVAL '5 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0005' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, created_at)
SELECT o.id, 'PREPAID', 'REFUNDED', 69.30, 'TXN-PRE-0006', NOW() - INTERVAL '4 days'  FROM orders o WHERE o.order_number = 'ORD-2026-0006' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'COD',     'PAID',    378.00, 'TXN-COD-0007', NOW() - INTERVAL '13 days',NOW() - INTERVAL '15 days' FROM orders o WHERE o.order_number = 'ORD-2026-0007' ON CONFLICT (order_id) DO NOTHING;
INSERT INTO payments (order_id, payment_method, status, amount, gateway_txn_ref, paid_at, created_at)
SELECT o.id, 'PREPAID', 'PAID',    115.50, 'TXN-PRE-0008', NOW() - INTERVAL '12 hours',NOW() - INTERVAL '1 day'  FROM orders o WHERE o.order_number = 'ORD-2026-0008' ON CONFLICT (order_id) DO NOTHING;
