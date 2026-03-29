-- Clean slate before seeding (safe to re-run)
TRUNCATE TABLE password_reset_tokens, refresh_tokens, users RESTART IDENTITY CASCADE;

-- Passwords are BCrypt of "Test@123"
INSERT INTO users (name, email, mobile, password_hash, role, status, created_at, updated_at) VALUES
  ('Admin User',    'admin@pharmacy.com', '9000000001', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'ADMIN',    'ACTIVE',    NOW(), NOW()),
  ('John Doe',      'john@example.com',   '9876543210', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Jane Smith',    'jane@example.com',   '9876543211', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Bob Suspended', 'bob@example.com',    '9876543212', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'SUSPENDED', NOW(), NOW()),
  ('Alice Brown',   'alice@example.com',  '9876543213', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW()),
  ('Charlie Dev',   'charlie@example.com','9876543214', '$2a$10$nxTirmFIIuFDAeE0rhFNUuSjOiuekk83C1FwYvAdN31ht9qFx/rM6', 'CUSTOMER', 'ACTIVE',    NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
