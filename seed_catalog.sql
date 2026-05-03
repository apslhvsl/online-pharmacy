-- ============================================================
-- Catalog seed — generated from output.csv
-- Categories: 1=Pain Relief, 2=Antibiotics, 3=Gastrointestinal,
--             4=Allergy, 5=Cold & Flu, 6=Chronic/Cardiac/Diabetes,
--             7=Vitamins & Supplements
-- Run AFTER Spring Boot has started (tables must exist)
-- ============================================================

TRUNCATE TABLE inventory_batches, medicines, categories RESTART IDENTITY CASCADE;

-- ── Categories ───────────────────────────────────────────────
INSERT INTO categories (name, slug, active, created_at) VALUES
  ('Pain Relief',            'pain-relief',          true, NOW()),
  ('Antibiotics',            'antibiotics',          true, NOW()),
  ('Gastrointestinal',       'gastrointestinal',     true, NOW()),
  ('Allergy',                'allergy',              true, NOW()),
  ('Cold & Flu',             'cold-flu',             true, NOW()),
  ('Chronic & Cardiac Care', 'chronic-cardiac-care', true, NOW()),
  ('Vitamins & Supplements', 'vitamins-supplements', true, NOW())
ON CONFLICT (slug) DO NOTHING;

-- ── Medicines ────────────────────────────────────────────────
INSERT INTO medicines (name, category_id, price, active, requires_prescription, manufacturer, strength, pack_size, description, image_url, reorder_level, created_at, updated_at) VALUES
  -- Pain Relief (cat 1)
  ('Paracetamol',       1,  25.00, true, false, 'Sun Pharma',  '500 mg',   '10 tablets', 'Pain reliever and fever reducer',       'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/c67db11e88874fdfab3c260e411ed30d.jpg',  50, NOW(), NOW()),
  ('Ibuprofen',         1,  40.00, true, false, 'Cipla',       '400 mg',   '10 tablets', 'Anti-inflammatory painkiller',          'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/yssbye7myd6dgld7wn2n.jpg',       40, NOW(), NOW()),
  ('Diclofenac',        1,  55.00, true, true,  'Dr Reddy''s', '50 mg',    '10 tablets', 'Strong pain relief',                    'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/946db7a8caa843f2b43e7f5f4cd4be40.jpg',  30, NOW(), NOW()),
  ('Aceclofenac',       1,  60.00, true, true,  'Lupin',       '100 mg',   '10 tablets', 'Pain relief and anti-inflammatory',     'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/kogynmu9hwmwgnfiibaa.jpg',       30, NOW(), NOW()),
  ('Chlorzoxazone',     1,  75.00, true, true,  'Sun Pharma',  '500 mg',   '10 tablets', 'Muscle relaxant',                       'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/mjkrkzsu8nkclm2nbzuo.jpg',       25, NOW(), NOW()),
  ('Diclofenac Gel',    1, 110.00, true, false, 'Sun Pharma',  '1%',       '30 g',       'Pain relief gel',                       'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/e4089d1abbca4939b9ef1fc620a3be67.jpg',  20, NOW(), NOW()),
  -- Antibiotics (cat 2)
  ('Amoxicillin',       2,  85.00, true, true,  'Cipla',       '500 mg',   '6 capsules', 'Antibiotic',                            'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/748f3ba153f24a9e86469f4a7e701d24.jpg',  30, NOW(), NOW()),
  ('Azithromycin',      2, 120.00, true, true,  'Sun Pharma',  '500 mg',   '3 tablets',  'Broad spectrum antibiotic',             'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/kqkouvaqejbyk47dvjfu.jpg',       25, NOW(), NOW()),
  ('Cefixime',          2, 140.00, true, true,  'Lupin',       '200 mg',   '10 tablets', 'Antibiotic',                            'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/e286fe5de6eb4463bf78b9f79fba9de9.jpg',  20, NOW(), NOW()),
  ('Doxycycline',       2,  90.00, true, true,  'Zydus',       '100 mg',   '10 capsules','Antibiotic',                            'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/hkcsim2wgxbpgoesgmj6.jpg',              25, NOW(), NOW()),
  ('Levofloxacin',      2, 150.00, true, true,  'Abbott',      '500 mg',   '10 tablets', 'Antibiotic',                            'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/twmkx9d51xn6hc77fhov.jpg',       20, NOW(), NOW()),
  ('Fluconazole',       2,  70.00, true, true,  'Abbott',      '150 mg',   '1 tablet',   'Antifungal',                            'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/u851yxmobzgert46yulm.jpg',       15, NOW(), NOW()),
  ('Clotrimazole Cream',2,  60.00, true, false, 'Cipla',       '1%',       '15 g',       'Antifungal cream',                      'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/fu5h6gvl6hok59hfqid9.jpg',       20, NOW(), NOW()),
  -- Gastrointestinal (cat 3)
  ('Omeprazole',        3,  60.00, true, false, 'Dr Reddy''s', '20 mg',    '10 capsules','Acidity treatment',                     'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/55dfce8bc94541e3bd2a06e0675af3b7.jpg',  35, NOW(), NOW()),
  ('Pantoprazole',      3,  70.00, true, false, 'Sun Pharma',  '40 mg',    '10 tablets', 'GERD treatment',                        'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/wvzx1hjqc87idlcawdip.jpg',       35, NOW(), NOW()),
  ('Rabeprazole',       3,  80.00, true, false, 'Cipla',       '20 mg',    '10 tablets', 'Acidity relief',                        'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/2d9939d3aca94e80aac2e1b1a1358438.jpg',  30, NOW(), NOW()),
  ('Digestive Enzyme Syrup', 3, 95.00, true, false, 'Zydus',   'NA',       '100 ml',     'Digestive enzyme',                      'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/tykerirlptjyvix7nhhn.jpg',       20, NOW(), NOW()),
  ('Antacid Suspension',3, 110.00, true, false, 'Dabur',       'NA',       '170 ml',     'Antacid',                               'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/tgtnmjdvhycowpzvbxvg.png',       20, NOW(), NOW()),
  ('ORS Powder',        3,  20.00, true, false, 'Dabur',       'NA',       '21 g',       'ORS solution',                          NULL,                                                                                                                                    50, NOW(), NOW()),
  ('Loperamide',        3,  35.00, true, false, 'Sun Pharma',  '2 mg',     '10 tablets', 'Anti-diarrheal',                        'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/ao1jmfsa2yiknvmpuak9.jpg',              30, NOW(), NOW()),
  ('Dicycloverine',     3,  60.00, true, true,  'Cipla',       '10 mg',    '10 tablets', 'Anti-spasmodic',                        NULL,                                                                                                                                    25, NOW(), NOW()),
  -- Allergy (cat 4)
  ('Cetirizine',        4,  30.00, true, false, 'Cipla',       '10 mg',    '10 tablets', 'Anti-allergic',                         'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/2bd4de82b17b47a585ccae45cf1ffafe.jpg',  40, NOW(), NOW()),
  ('Levocetirizine',    4,  45.00, true, false, 'Sun Pharma',  '5 mg',     '10 tablets', 'Anti-allergic',                         'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/0b3747a017724731a99599f36d7d6d6e.jpg',  35, NOW(), NOW()),
  ('Fexofenadine',      4, 120.00, true, false, 'Lupin',       '120 mg',   '10 tablets', 'Anti-allergic',                         'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/0651aa0f7fb04232b15ecff2a9e61f4c.jpg',  25, NOW(), NOW()),
  ('Chlorpheniramine',  4,  20.00, true, false, 'Zydus',       '4 mg',     '10 tablets', 'Cold relief',                           'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/27e36dc977f644c09c7b391385c2f09c.jpg',  40, NOW(), NOW()),
  ('Anti Allergy Syrup',4,  85.00, true, false, 'Cipla',       'NA',       '100 ml',     'Allergy relief syrup',                  'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/ca61d763e2934dc799268d045031d035.jpg',  20, NOW(), NOW()),
  ('Eye Lubricant Drops',4, 120.00, true, false,'Sun Pharma',  'NA',       '10 ml',      'Eye drops',                             'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/i09dq8qmfe8keusgoybt.jpg',              20, NOW(), NOW()),
  ('Ear Drops',         4,  90.00, true, false, 'Cipla',       'NA',       '10 ml',      'Ear drops',                             'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/50a28796abca4ad69b1e907f8120eb05.jpg',  20, NOW(), NOW()),
  -- Cold & Flu (cat 5)
  ('Cough Syrup',       5,  90.00, true, false, 'Dabur',       'NA',       '100 ml',     'Cough suppressant',                     NULL,                                                                                                                                    20, NOW(), NOW()),
  ('Ambroxol Syrup',    5,  95.00, true, false, 'Sun Pharma',  '30 mg',    '100 ml',     'Expectorant',                           'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/1e05568d8ca649929760131c44a7c8dd.jpg',  20, NOW(), NOW()),
  ('Phenylephrine',     5,  50.00, true, false, 'Cipla',       '10 mg',    '10 tablets', 'Cold relief',                           'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/1a5cf5a671a34765a9f60f944265c2dd.jpg',  25, NOW(), NOW()),
  ('Nasal Spray',       5, 120.00, true, false, 'Zydus',       'NA',       '10 ml',      'Nasal decongestant',                    'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/619669e5e6c94fc38914b5fadbf8dd00.jpg',  15, NOW(), NOW()),
  ('Herbal Cough Syrup',5, 110.00, true, false, 'Himalaya',    'NA',       '100 ml',     'Cough relief',                          NULL,                                                                                                                                    20, NOW(), NOW()),
  ('Dettol Liquid',     5,  70.00, true, false, 'Reckitt',     'NA',       '100 ml',     'Antiseptic',                            'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/7cf780a7d2f74657a79d751343d61fff.jpg',  20, NOW(), NOW()),
  ('Soframycin Cream',  5,  95.00, true, false, 'GSK',         'NA',       '30 g',       'Antiseptic cream',                      'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/e4089d1abbca4939b9ef1fc620a3be67.jpg',  20, NOW(), NOW()),
  -- Chronic & Cardiac Care (cat 6)
  ('Metformin',         6,  55.00, true, true,  'Sun Pharma',  '500 mg',   '10 tablets', 'Diabetes control',                      'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/ec261a22df1d49c8aaa1790a735a868e.jpg',  60, NOW(), NOW()),
  ('Glimepiride',       6,  65.00, true, true,  'Cipla',       '1 mg',     '10 tablets', 'Diabetes control',                      'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/v9mscy7flmdd2vcxsuzc.jpg',              50, NOW(), NOW()),
  ('Amlodipine',        6,  45.00, true, true,  'Lupin',       '5 mg',     '10 tablets', 'Blood pressure',                        'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/276c276ba0c94e0d99376c98295d8024.jpg',  45, NOW(), NOW()),
  ('Telmisartan',       6, 110.00, true, true,  'Sun Pharma',  '40 mg',    '10 tablets', 'Hypertension',                          'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/098efd0233f445b99635b584b3ba0ec3.jpg',  35, NOW(), NOW()),
  ('Atorvastatin',      6,  90.00, true, true,  'Dr Reddy''s', '10 mg',    '10 tablets', 'Cholesterol control',                   'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/f5888caa9086409c9a1bde789bbb9761.jpg',  35, NOW(), NOW()),
  ('Salbutamol Inhaler',6, 180.00, true, true,  'Cipla',       '100 mcg',  '200 doses',  'Asthma inhaler',                        'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/9117d15a82fc4d41889e083e295d7de9.jpg',  15, NOW(), NOW()),
  ('Budesonide Inhaler',6, 250.00, true, true,  'Lupin',       '200 mcg',  '200 doses',  'Steroid inhaler',                       'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/95dfc22ff9684cf493f5250d28a2af40.jpg',  10, NOW(), NOW()),
  ('Thyroxine',         6, 100.00, true, true,  'Abbott',      '50 mcg',   '10 tablets', 'Thyroid medicine',                      'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/a505c8250cb54e038c0383ec03665d1c.jpg',  20, NOW(), NOW()),
  ('Alprazolam',        6,  85.00, true, true,  'Sun Pharma',  '0.5 mg',   '10 tablets', 'Anti-anxiety',                          'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/a5c1bb686b5a4e3780ed4adda10d623f.jpg',  20, NOW(), NOW()),
  ('Zolpidem',          6, 120.00, true, true,  'Cipla',       '10 mg',    '10 tablets', 'Sleep aid',                             'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/6a2dae0b8d0e47daa157c3937041bf8f.jpg',  15, NOW(), NOW()),
  -- Vitamins & Supplements (cat 7)
  ('Vitamin C',         7, 120.00, true, false, 'Himalaya',    '500 mg',   '15 tablets', 'Vitamin supplement',                    'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/cropped/uedj79hfzy4z8j1rokkw.jpg',       25, NOW(), NOW()),
  ('Multivitamin Tablets',7,150.00, true, false,'Zydus',       'NA',       '15 tablets', 'Multivitamin',                          NULL,                                                                                                                                    25, NOW(), NOW()),
  ('Vitamin D3',        7,  80.00, true, false, 'Sun Pharma',  '60000 IU', '4 capsules', 'Vitamin D supplement',                  'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/0ee65599098d4c229b46397dae778930.jpg',  20, NOW(), NOW()),
  ('Calcium Tablets',   7, 130.00, true, false, 'Cipla',       '500 mg',   '15 tablets', 'Calcium supplement',                    'https://onemg.gumlet.io/l_watermark_346,w_380,h_380/a_ignore,w_380,h_380,c_fit,q_auto,f_auto/106cd9f4992a48cfae6d740b66aecd2e.jpg',  25, NOW(), NOW()),
  ('Iron Folic Acid',   7,  95.00, true, false, 'Lupin',       'NA',       '15 tablets', 'Iron supplement',                       NULL,                                                                                                                                    25, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ── Inventory Batches ────────────────────────────────────────
-- One or two batches per medicine, using medicine name lookup

-- Pain Relief
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PCM-B001', 25.00, 150, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PCM-B002', 25.00, 100, '2028-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Paracetamol';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IBU-B001', 40.00, 120, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IBU-B002', 40.00,  80, '2028-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Ibuprofen';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DIC-B001', 55.00,  60, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Diclofenac';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ACE-B001', 60.00,  50, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Aceclofenac';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CHZ-B001', 75.00,  40, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Chlorzoxazone';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DGE-B001', 110.00, 30, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Diclofenac Gel';

-- Antibiotics
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AMX-B001', 85.00, 100, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Amoxicillin';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AMX-B002', 85.00,  60, '2028-01-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Amoxicillin';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AZI-B001', 120.00, 70, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Azithromycin';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CEF-B001', 140.00, 50, '2027-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Cefixime';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DOX-B001',  90.00, 60, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Doxycycline';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'LEV-B001', 150.00, 40, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Levofloxacin';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'FLU-B001',  70.00, 30, '2027-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Fluconazole';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CLO-B001',  60.00, 45, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Clotrimazole Cream';

-- Gastrointestinal
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'OMP-B001',  60.00, 90, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Omeprazole';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'OMP-B002',  60.00, 60, '2028-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Omeprazole';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PAN-B001',  70.00,  8, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Pantoprazole';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'RAB-B001',  80.00, 55, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Rabeprazole';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DES-B001',  95.00, 35, '2027-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Digestive Enzyme Syrup';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ANT-B001', 110.00, 40, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Antacid Suspension';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ORS-B001',  20.00, 200, '2027-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'ORS Powder';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'LOP-B001',  35.00, 70, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Loperamide';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DCY-B001',  60.00, 45, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Dicycloverine';

-- Allergy
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CET-B001',  30.00, 180, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Cetirizine';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CET-B002',  30.00, 120, '2028-03-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Cetirizine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'LVC-B001',  45.00,  80, '2027-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Levocetirizine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'FEX-B001', 120.00,  50, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Fexofenadine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CHP-B001',  20.00, 100, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Chlorpheniramine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AAS-B001',  85.00,  35, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Anti Allergy Syrup';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'EYE-B001', 120.00,  40, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Eye Lubricant Drops';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'EAR-B001',  90.00,  35, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Ear Drops';

-- Cold & Flu
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CGH-B001',  90.00,  60, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Cough Syrup';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AMB-B001',  95.00,  55, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Ambroxol Syrup';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'PHE-B001',  50.00,  70, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Phenylephrine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'NSL-B001', 120.00,  30, '2027-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Nasal Spray';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'HCS-B001', 110.00,  45, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Herbal Cough Syrup';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'DTL-B001',  70.00,  50, '2027-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Dettol Liquid';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'SFM-B001',  95.00,  40, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Soframycin Cream';

-- Chronic & Cardiac Care
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MET-B001',  55.00, 130, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MET-B002',  55.00,  90, '2028-02-28', NOW(), NOW() FROM medicines m WHERE m.name = 'Metformin';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'GLI-B001',  65.00,  70, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Glimepiride';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'AML-B001',  45.00, 100, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Amlodipine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'TEL-B001', 110.00,  55, '2027-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Telmisartan';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ATO-B001',  90.00,  75, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Atorvastatin';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'SAL-B001', 180.00,  25, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Salbutamol Inhaler';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'BUD-B001', 250.00,  15, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Budesonide Inhaler';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'THY-B001', 100.00,  50, '2027-12-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Thyroxine';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ALP-B001',  85.00,  30, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Alprazolam';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'ZOL-B001', 120.00,  20, '2027-07-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Zolpidem';

-- Vitamins & Supplements
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTC-B001', 120.00,  90, '2027-11-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C';
INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTC-B002', 120.00,  60, '2028-05-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin C';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'MVT-B001', 150.00,  70, '2027-08-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Multivitamin Tablets';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'VTD-B001',  80.00,  55, '2027-06-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Vitamin D3';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'CAL-B001', 130.00,  65, '2027-10-31', NOW(), NOW() FROM medicines m WHERE m.name = 'Calcium Tablets';

INSERT INTO inventory_batches (medicine_id, batch_number, price, quantity, expiry_date, created_at, updated_at)
SELECT m.id, 'IFA-B001',  95.00,  80, '2027-09-30', NOW(), NOW() FROM medicines m WHERE m.name = 'Iron Folic Acid';
