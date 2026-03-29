-- Clean slate before seeding (safe to re-run)
TRUNCATE TABLE inventory_batches, medicines, categories RESTART IDENTITY CASCADE;

INSERT INTO categories (name, slug, active, created_at) VALUES
  ('Pain Relief',            'pain-relief',          true, NOW()),
  ('Vitamins & Supplements', 'vitamins-supplements', true, NOW()),
  ('Antibiotics',            'antibiotics',          true, NOW()),
  ('Diabetes Care',          'diabetes-care',        true, NOW()),
  ('Cold & Flu',             'cold-flu',             true, NOW()),
  ('Cardiac Care',           'cardiac-care',         true, NOW()),
  ('Gastrointestinal',       'gastrointestinal',     true, NOW())
ON CONFLICT (slug) DO NOTHING;

INSERT INTO medicines (name, category_id, price, active, requires_prescription, manufacturer, strength, pack_size, description, reorder_level, created_at, updated_at) VALUES
  ('Paracetamol 500mg',   1,  25.00, true,  false, 'GSK',        '500mg',    '10 tablets',  'Pain reliever and fever reducer',             20, NOW(), NOW()),
  ('Ibuprofen 400mg',     1,  45.00, true,  false, 'Abbott',     '400mg',    '10 tablets',  'Anti-inflammatory pain reliever',             15, NOW(), NOW()),
  ('Aspirin 75mg',        1,  18.00, true,  false, 'USV',        '75mg',     '14 tablets',  'Low-dose aspirin for heart health',           10, NOW(), NOW()),
  ('Diclofenac 50mg',     1,  38.00, true,  false, 'Novartis',   '50mg',     '10 tablets',  'NSAID for pain and inflammation',             10, NOW(), NOW()),
  ('Vitamin C 500mg',     2,  35.00, true,  false, 'Abbott',     '500mg',    '15 tablets',  'Immunity booster vitamin C',                  30, NOW(), NOW()),
  ('Vitamin D3 1000IU',   2, 120.00, true,  false, 'Sun Pharma', '1000IU',   '30 capsules', 'Vitamin D3 supplement',                       10, NOW(), NOW()),
  ('Multivitamin Daily',  2, 180.00, true,  false, 'Bayer',      'N/A',      '30 tablets',  'Complete daily multivitamin',                 10, NOW(), NOW()),
  ('Omega-3 1000mg',      2,  95.00, true,  false, 'Himalaya',   '1000mg',   '30 capsules', 'Fish oil omega-3 supplement',                 10, NOW(), NOW()),
  ('Amoxicillin 500mg',   3,  85.00, true,  true,  'Cipla',      '500mg',    '10 capsules', 'Broad-spectrum antibiotic',                   15, NOW(), NOW()),
  ('Azithromycin 500mg',  3,  95.00, true,  true,  'Alembic',    '500mg',    '3 tablets',   'Antibiotic for respiratory infections',       10, NOW(), NOW()),
  ('Ciprofloxacin 500mg', 3, 110.00, true,  true,  'Cipla',      '500mg',    '10 tablets',  'Fluoroquinolone antibiotic',                  10, NOW(), NOW()),
  ('Metformin 500mg',     4,  30.00, true,  true,  'USV',        '500mg',    '20 tablets',  'Oral diabetes medication',                    20, NOW(), NOW()),
  ('Glimepiride 2mg',     4,  65.00, true,  true,  'Sanofi',     '2mg',      '10 tablets',  'Sulfonylurea for type 2 diabetes',            10, NOW(), NOW()),
  ('Insulin Glargine',    4, 850.00, true,  true,  'Sanofi',     '100IU/ml', '1 vial',      'Long-acting insulin for diabetes',             5, NOW(), NOW()),
  ('Cetirizine 10mg',     5,  22.00, true,  false, 'Cipla',      '10mg',     '10 tablets',  'Antihistamine for allergy and cold',          20, NOW(), NOW()),
  ('Dextromethorphan',    5,  55.00, true,  false, 'J&J',        '10mg/5ml', '100ml',       'Cough suppressant syrup',                      5, NOW(), NOW()),
  ('Phenylephrine 10mg',  5,  30.00, true,  false, 'GSK',        '10mg',     '10 tablets',  'Nasal decongestant',                          10, NOW(), NOW()),
  ('Atorvastatin 10mg',   6,  75.00, true,  true,  'Pfizer',     '10mg',     '10 tablets',  'Statin for cholesterol management',           15, NOW(), NOW()),
  ('Amlodipine 5mg',      6,  45.00, true,  true,  'Pfizer',     '5mg',      '10 tablets',  'Calcium channel blocker for hypertension',   15, NOW(), NOW()),
  ('Pantoprazole 40mg',   7,  48.00, true,  false, 'Sun Pharma', '40mg',     '15 tablets',  'Proton pump inhibitor for acidity',           20, NOW(), NOW()),
  ('Ranitidine 150mg',    7,  20.00, true,  false, 'GSK',        '150mg',    '10 tablets',  'H2 blocker for heartburn',                    10, NOW(), NOW()),
  ('Domperidone 10mg',    7,  28.00, true,  false, 'Cipla',      '10mg',     '10 tablets',  'Anti-nausea and motility agent',              10, NOW(), NOW()),
  ('Discontinued Drug',   1,  10.00, false, false, 'Unknown',    '10mg',     '10 tablets',  'Discontinued - should not appear in catalog',  0, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PCM-B001', 25.00, 100, '2026-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PCM-B002', 26.00, 150, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol 500mg';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IBU-B001', 45.00, 80, '2026-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen 400mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IBU-B002', 44.00, 120, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen 400mg';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ASP-B001', 18.00, 80, '2026-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Aspirin 75mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DIC-B001', 38.00, 60, '2027-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Diclofenac 50mg';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTC-B001', 35.00, 200, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTC-B002', 34.00, 100, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C 500mg';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTD-B001', 120.00, 90, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin D3 1000IU';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MUL-B001', 180.00, 60, '2027-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Multivitamin Daily';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'OMG-B001', 95.00, 45, '2027-04-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Omega-3 1000mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AMX-B001', 85.00, 100, '2026-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Amoxicillin 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AZI-B001', 95.00, 70, '2026-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Azithromycin 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CIP-B001', 110.00, 50, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Ciprofloxacin 500mg';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MET-B001', 30.00, 120, '2027-04-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin 500mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MET-B002', 29.00, 80, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin 500mg';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'GLI-B001', 65.00, 50, '2026-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Glimepiride 2mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'INS-B001', 850.00, 3, '2026-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Insulin Glargine';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CET-B001', 22.00, 180, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Cetirizine 10mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DEX-B001', 55.00, 40, '2026-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Dextromethorphan';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PHE-B001', 30.00, 75, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Phenylephrine 10mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ATO-B001', 75.00, 90, '2027-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Atorvastatin 10mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AML-B001', 45.00, 110, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Amlodipine 5mg';
-- LOW STOCK
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PAN-B001', 48.00, 5, '2027-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Pantoprazole 40mg';
-- EXPIRING SOON
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'RAN-B001', 20.00, 30, '2026-05-15', NOW(), NOW() FROM medicines m WHERE m.name = 'Ranitidine 150mg';
-- VALID + ALREADY EXPIRED batch
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DOM-B001', 28.00, 60, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Domperidone 10mg';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DOM-B000', 25.00, 10, '2025-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Domperidone 10mg';
