# Pharmacy API Documentation

Base URL: `http://localhost:8080`

All authenticated endpoints require a Bearer token in the `Authorization` header.
The gateway injects `X-User-Id` automatically from the JWT — do not send it manually.

---

## Auth Service `/api/auth`

### POST /api/auth/signup
Register a new customer account.

**Conditions to test:**
- ✅ Valid payload → 201 Created + tokens
- ❌ Missing name/email/mobile/password → 400
- ❌ Weak password (no uppercase/digit/special char) → 400
- ❌ Duplicate email → 409

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "mobile": "9876543210",
    "password": "Secret@123"
  }'
```

---

### POST /api/auth/login
Authenticate and receive JWT + refresh token.

**Conditions to test:**
- ✅ Valid credentials → 200 + accessToken + refreshToken
- ❌ Wrong password → 401
- ❌ Non-existent email → 401
- ❌ Suspended account → 403

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Secret@123"
  }'
```

---

### POST /api/auth/refresh
Get a new access token using a refresh token.

**Conditions to test:**
- ✅ Valid refresh token → 200 + new tokens
- ❌ Expired/invalid refresh token → 401
- ❌ Missing token field → 400

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<your_refresh_token>"}'
```

---

### POST /api/auth/forgot-password
Request a password reset link.

**Conditions to test:**
- ✅ Registered email → 200 (always returns same message for security)
- ✅ Unknown email → 200 (same response, no leak)
- ❌ Invalid email format → 400

```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com"}'
```

---

### POST /api/auth/reset-password
Reset password using the token from the email link.

**Conditions to test:**
- ✅ Valid token + strong password → 200
- ❌ Expired/invalid token → 400
- ❌ Weak new password → 400
- ❌ Missing fields → 400

```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<reset_token>",
    "newPassword": "NewSecret@456"
  }'
```

---

### POST /api/auth/logout
Invalidate the current user's refresh token. Requires auth.

**Conditions to test:**
- ✅ Valid JWT → 204 No Content
- ❌ No/invalid token → 401

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <access_token>"
```

---

### GET /api/auth/me
Get the authenticated user's profile.

**Conditions to test:**
- ✅ Valid JWT → 200 + profile object
- ❌ No token → 401
- ❌ Expired token → 401

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>"
```

---

### PUT /api/auth/me
Update name and mobile number.

**Conditions to test:**
- ✅ Valid payload → 200 + updated profile
- ❌ Blank name or mobile → 400
- ❌ No token → 401

```bash
curl -X PUT http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Jane Doe", "mobile": "9123456789"}'
```

---

### POST /api/auth/change-password
Change password while logged in.

**Conditions to test:**
- ✅ Correct current password + strong new password → 200
- ❌ Wrong current password → 400/401
- ❌ Weak new password → 400
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "Secret@123",
    "newPassword": "NewSecret@456"
  }'
```

---

## Catalog Service `/api/catalog`

### GET /api/catalog/categories
List all active categories. Public endpoint.

**Conditions to test:**
- ✅ No params → 200 + array of categories
- ✅ Empty DB → 200 + empty array

```bash
curl http://localhost:8080/api/catalog/categories
```

---

### GET /api/catalog/categories/{id}
Get a single category by ID.

**Conditions to test:**
- ✅ Valid ID → 200 + category object
- ❌ Non-existent ID → 404

```bash
curl http://localhost:8080/api/catalog/categories/1
```

---

### GET /api/catalog/medicines
Paginated medicine listing with filters.

**Conditions to test:**
- ✅ No params → 200 + first page (size 10)
- ✅ `?q=paracetamol` → filters by name/ingredient
- ✅ `?categoryId=2` → filters by category
- ✅ `?requiresPrescription=true` → prescription-only medicines
- ✅ `?inStock=true` → only in-stock items
- ✅ `?minPrice=10&maxPrice=100` → price range filter
- ✅ `?page=1&size=5` → pagination
- ❌ Invalid `minPrice` (non-numeric) → 400

```bash
# Basic listing
curl "http://localhost:8080/api/catalog/medicines"

# With filters
curl "http://localhost:8080/api/catalog/medicines?q=paracetamol&inStock=true&minPrice=5&maxPrice=200&page=0&size=10"
```

---

### GET /api/catalog/medicines/featured
Get featured medicines list.

**Conditions to test:**
- ✅ → 200 + array (may be empty)

```bash
curl http://localhost:8080/api/catalog/medicines/featured
```

---

### GET /api/catalog/medicines/{id}
Get a single medicine by ID.

**Conditions to test:**
- ✅ Valid ID → 200 + medicine object
- ❌ Non-existent ID → 404

```bash
curl http://localhost:8080/api/catalog/medicines/1
```

---

### GET /api/catalog/medicines/{id}/stock-check
Check if a quantity is available for a medicine.

**Conditions to test:**
- ✅ Sufficient stock → 200 `{"available": true}`
- ✅ Insufficient stock → 200 `{"available": false}`
- ❌ Missing `quantity` param → 400
- ❌ Non-existent medicine ID → 404

```bash
curl "http://localhost:8080/api/catalog/medicines/1/stock-check?quantity=5"
```

---

### POST /api/catalog/prescriptions/upload
Upload a prescription image. Requires auth (customer).

**Conditions to test:**
- ✅ Valid image (jpg/png/pdf) → 201 + prescription object
- ❌ Invalid file type → 400
- ❌ No file attached → 400
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/catalog/prescriptions/upload \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@/path/to/prescription.jpg"
```

---

### GET /api/catalog/prescriptions
Get all prescriptions for the logged-in user.

**Conditions to test:**
- ✅ Valid token → 200 + array
- ✅ No prescriptions yet → 200 + empty array
- ❌ No token → 401

```bash
curl http://localhost:8080/api/catalog/prescriptions \
  -H "Authorization: Bearer <access_token>"
```

---

### GET /api/catalog/prescriptions/{id}
Get a specific prescription (ownership enforced).

**Conditions to test:**
- ✅ Own prescription → 200
- ❌ Another user's prescription → 403/404
- ❌ Non-existent ID → 404
- ❌ No token → 401

```bash
curl http://localhost:8080/api/catalog/prescriptions/1 \
  -H "Authorization: Bearer <access_token>"
```

---

### GET /api/catalog/prescriptions/{id}/file
Download the prescription file.

**Conditions to test:**
- ✅ Own prescription → 200 + file stream
- ❌ Another user's prescription → 403/404
- ❌ No token → 401

```bash
curl http://localhost:8080/api/catalog/prescriptions/1/file \
  -H "Authorization: Bearer <access_token>" \
  --output prescription.jpg
```

---

## Order Service `/api/orders`

### GET /api/orders/cart
Get the current user's cart.

**Conditions to test:**
- ✅ Valid token → 200 + cart (empty or with items)
- ❌ No token → 401

```bash
curl http://localhost:8080/api/orders/cart \
  -H "Authorization: Bearer <access_token>"
```

---

### POST /api/orders/cart/items
Add an item to the cart.

**Conditions to test:**
- ✅ Valid batchId + quantity → 200 + updated cart
- ❌ batchId not found → 404
- ❌ quantity < 1 → 400
- ❌ Insufficient stock → 400
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/orders/cart/items \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"batchId": 1, "quantity": 2}'
```

---

### PUT /api/orders/cart/items/{batchId}
Update quantity of a cart item.

**Conditions to test:**
- ✅ Valid quantity → 200 + updated cart
- ❌ quantity < 1 → 400
- ❌ batchId not in cart → 404
- ❌ No token → 401

```bash
curl -X PUT http://localhost:8080/api/orders/cart/items/1 \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 3}'
```

---

### DELETE /api/orders/cart/items/{batchId}
Remove a specific item from the cart.

**Conditions to test:**
- ✅ Item exists in cart → 200 + updated cart
- ❌ Item not in cart → 404
- ❌ No token → 401

```bash
curl -X DELETE http://localhost:8080/api/orders/cart/items/1 \
  -H "Authorization: Bearer <access_token>"
```

---

### DELETE /api/orders/cart
Clear the entire cart.

**Conditions to test:**
- ✅ → 204 No Content
- ❌ No token → 401

```bash
curl -X DELETE http://localhost:8080/api/orders/cart \
  -H "Authorization: Bearer <access_token>"
```

---

### POST /api/orders/checkout/start
Step 1 — Initiate checkout from cart.

**Conditions to test:**
- ✅ Non-empty cart → 201 + checkout session with orderId
- ❌ Empty cart → 400
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/orders/checkout/start \
  -H "Authorization: Bearer <access_token>"
```

---

### POST /api/orders/checkout/{orderId}/address
Step 2 — Set delivery address (use saved addressId or inline address).

**Conditions to test:**
- ✅ Valid saved addressId → 200 + order
- ✅ Inline address object → 200 + order
- ❌ addressId not belonging to user → 403
- ❌ Missing required address fields (line1, city, state, pincode) → 400
- ❌ No token → 401

```bash
# Using saved address
curl -X POST http://localhost:8080/api/orders/checkout/1/address \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"addressId": 2}'

# Using inline address
curl -X POST http://localhost:8080/api/orders/checkout/1/address \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "inlineAddress": {
      "label": "Home",
      "line1": "123 Main Street",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001"
    }
  }'
```

---

### POST /api/orders/checkout/{orderId}/prescription-link
Step 3 — Attach an approved prescription to the order (if required).

**Conditions to test:**
- ✅ Approved prescription belonging to user → 200
- ❌ Prescription not approved → 400
- ❌ Prescription belongs to another user → 403
- ❌ No token → 401

```bash
curl -X POST "http://localhost:8080/api/orders/checkout/1/prescription-link?prescriptionId=3" \
  -H "Authorization: Bearer <access_token>"
```

---

### POST /api/orders/checkout/{orderId}/confirm
Step 4 — Confirm the order.

**Conditions to test:**
- ✅ All steps complete → 200 + confirmed order
- ❌ Address not set → 400
- ❌ Prescription required but not linked → 400
- ❌ Stock changed since checkout → 400
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/orders/checkout/1/confirm \
  -H "Authorization: Bearer <access_token>"
```

---

### GET /api/orders
Get the current user's order history.

**Conditions to test:**
- ✅ No params → 200 + paginated orders
- ✅ `?status=DELIVERED` → filtered by status
- ✅ `?page=0&size=5` → pagination
- ❌ No token → 401

Valid status values: `DRAFT, CHECKOUT_STARTED, PRESCRIPTION_PENDING, PRESCRIPTION_APPROVED, PRESCRIPTION_REJECTED, PAYMENT_PENDING, PAID, PACKED, OUT_FOR_DELIVERY, DELIVERED, CUSTOMER_CANCELLED, ADMIN_CANCELLED, PAYMENT_FAILED, RETURN_REQUESTED, REFUND_INITIATED, REFUND_COMPLETED`

```bash
curl "http://localhost:8080/api/orders?status=DELIVERED&page=0&size=10" \
  -H "Authorization: Bearer <access_token>"
```

---

### GET /api/orders/{id}
Get a specific order (ownership enforced).

**Conditions to test:**
- ✅ Own order → 200 + order detail
- ❌ Another user's order → 404
- ❌ Non-existent ID → 404
- ❌ No token → 401

```bash
curl http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer <access_token>"
```

---

### PATCH /api/orders/{id}/cancel
Cancel an order (customer).

**Conditions to test:**
- ✅ Cancellable status (e.g. PAYMENT_PENDING, PAID) → 200
- ❌ Already delivered/cancelled → 400
- ❌ Another user's order → 404
- ❌ No token → 401

```bash
curl -X PATCH http://localhost:8080/api/orders/1/cancel \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Changed my mind"}'
```

---

### POST /api/orders/{id}/reorder
Re-add all items from a past order into the cart.

**Conditions to test:**
- ✅ Valid past order → 200 + updated cart (out-of-stock items silently skipped)
- ❌ Another user's order → 404
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/orders/1/reorder \
  -H "Authorization: Bearer <access_token>"
```

---

### POST /api/orders/{id}/return
Request a return for a delivered order.

**Conditions to test:**
- ✅ DELIVERED order → 200 + order with RETURN_REQUESTED status
- ❌ Non-delivered order → 400
- ❌ Another user's order → 404
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/orders/1/return \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Wrong item received"}'
```

---

### POST /api/orders/payments/initiate
Initiate payment for a confirmed order.

**Conditions to test:**
- ✅ Valid orderId + paymentMethod → 200 + payment object
- ❌ Order not in PAYMENT_PENDING state → 400
- ❌ Invalid paymentMethod → 400
- ❌ No token → 401

Valid paymentMethod values: `COD`, `PREPAID`, `WALLET`

```bash
curl -X POST http://localhost:8080/api/orders/payments/initiate \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1, "paymentMethod": "COD"}'
```

---

### POST /api/orders/payments/callback
Payment gateway callback (called by payment provider).

**Conditions to test:**
- ✅ `success=true` → order moves to PAID
- ✅ `success=false` → order moves to PAYMENT_FAILED
- ❌ Invalid txnRef → 404

```bash
curl -X POST "http://localhost:8080/api/orders/payments/callback?txnRef=TXN123&success=true"
```

---

### GET /api/orders/payments/{orderId}
Get payment details for an order.

**Conditions to test:**
- ✅ Own order with payment → 200 + payment object
- ❌ Another user's order → 404
- ❌ No payment initiated yet → 404
- ❌ No token → 401

```bash
curl http://localhost:8080/api/orders/payments/1 \
  -H "Authorization: Bearer <access_token>"
```

---

## Addresses `/api/orders/addresses`

### GET /api/orders/addresses
Get all saved addresses for the user.

**Conditions to test:**
- ✅ Valid token → 200 + array
- ✅ No addresses saved → 200 + empty array
- ❌ No token → 401

```bash
curl http://localhost:8080/api/orders/addresses \
  -H "Authorization: Bearer <access_token>"
```

---

### POST /api/orders/addresses
Add a new address.

**Conditions to test:**
- ✅ Valid payload → 201 + address object
- ❌ Missing line1/city/state/pincode → 400
- ❌ No token → 401

```bash
curl -X POST http://localhost:8080/api/orders/addresses \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home",
    "line1": "123 Main Street",
    "line2": "Apt 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "isDefault": true
  }'
```

---

### PUT /api/orders/addresses/{id}
Update an existing address.

**Conditions to test:**
- ✅ Own address + valid payload → 200
- ❌ Another user's address → 403/404
- ❌ Missing required fields → 400
- ❌ No token → 401

```bash
curl -X PUT http://localhost:8080/api/orders/addresses/1 \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Office",
    "line1": "456 Business Park",
    "city": "Pune",
    "state": "Maharashtra",
    "pincode": "411001"
  }'
```

---

### DELETE /api/orders/addresses/{id}
Delete a saved address.

**Conditions to test:**
- ✅ Own address → 204 No Content
- ❌ Another user's address → 403/404
- ❌ No token → 401

```bash
curl -X DELETE http://localhost:8080/api/orders/addresses/1 \
  -H "Authorization: Bearer <access_token>"
```

---

## Admin Service `/api/admin`

> All admin endpoints require a JWT belonging to a user with the `ADMIN` role.

---

### GET /api/admin/dashboard
Get order/revenue summary dashboard.

**Conditions to test:**
- ✅ Admin token → 200 + dashboard stats
- ❌ Customer token → 403
- ❌ No token → 401

```bash
curl http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/reports/sales
Get sales report.

**Conditions to test:**
- ✅ Admin token → 200 + sales data
- ❌ Non-admin → 403

```bash
curl http://localhost:8080/api/admin/reports/sales \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/medicines
List all medicines (admin view, includes inactive).

**Conditions to test:**
- ✅ Admin token → 200 + full list
- ❌ Non-admin → 403

```bash
curl http://localhost:8080/api/admin/medicines \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/medicines/{id}
Get medicine by ID.

**Conditions to test:**
- ✅ Valid ID → 200
- ❌ Non-existent ID → 404

```bash
curl http://localhost:8080/api/admin/medicines/1 \
  -H "Authorization: Bearer <admin_token>"
```

---

### POST /api/admin/medicines
Create a new medicine.

**Conditions to test:**
- ✅ Full valid payload → 201 + medicine
- ❌ Non-existent categoryId → 404
- ❌ Missing required fields → 400
- ❌ Non-admin → 403

```bash
curl -X POST http://localhost:8080/api/admin/medicines \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paracetamol 500mg",
    "brandName": "Calpol",
    "activeIngredient": "Paracetamol",
    "categoryId": 1,
    "price": 25.00,
    "mrp": 30.00,
    "stock": 200,
    "reorderLevel": 20,
    "requiresPrescription": false,
    "dosageForm": "Tablet",
    "strength": "500mg",
    "packSize": "10 tablets",
    "description": "Pain reliever and fever reducer",
    "manufacturer": "GSK",
    "expiryDate": "2026-12-31",
    "isFeatured": false
  }'
```

---

### PUT /api/admin/medicines/{id}
Update a medicine.

**Conditions to test:**
- ✅ Valid ID + payload → 200
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl -X PUT http://localhost:8080/api/admin/medicines/1 \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paracetamol 500mg",
    "price": 22.00,
    "mrp": 30.00,
    "stock": 150,
    "categoryId": 1,
    "requiresPrescription": false,
    "isFeatured": true
  }'
```

---

### PATCH /api/admin/medicines/{id}/stock
Adjust stock for a medicine (positive = add, negative = reduce).

**Conditions to test:**
- ✅ Positive adjustment → 200 + updated stock
- ✅ Negative adjustment within available stock → 200
- ❌ Negative adjustment exceeding stock → 400
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl -X PATCH http://localhost:8080/api/admin/medicines/1/stock \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"adjustment": 50, "reason": "New stock received"}'
```

---

### PATCH /api/admin/medicines/{id}/deactivate
Deactivate a medicine (soft delete).

**Conditions to test:**
- ✅ Active medicine → 200 + deactivated
- ❌ Already inactive → 400
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl -X PATCH http://localhost:8080/api/admin/medicines/1/deactivate \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/medicines/low-stock
Get medicines below reorder level.

**Conditions to test:**
- ✅ No params → 200 + list using default reorder level
- ✅ `?stockLessThan=10` → custom threshold
- ❌ Non-admin → 403

```bash
curl "http://localhost:8080/api/admin/medicines/low-stock?stockLessThan=10" \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/medicines/expiring
Get medicines expiring soon.

**Conditions to test:**
- ✅ No params → 200 + expiring within 90 days (default)
- ✅ `?days=30` → expiring within 30 days
- ✅ `?expiryBefore=2026-06-01` → expiring before specific date
- ❌ Non-admin → 403

```bash
curl "http://localhost:8080/api/admin/medicines/expiring?days=30" \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/categories
List all categories (admin view).

**Conditions to test:**
- ✅ Admin token → 200 + list
- ❌ Non-admin → 403

```bash
curl http://localhost:8080/api/admin/categories \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/categories/{id}
Get a category by ID.

**Conditions to test:**
- ✅ Valid ID → 200
- ❌ Non-existent ID → 404

```bash
curl http://localhost:8080/api/admin/categories/1 \
  -H "Authorization: Bearer <admin_token>"
```

---

### POST /api/admin/categories
Create a new category.

**Conditions to test:**
- ✅ Valid payload → 201 + category
- ❌ Duplicate slug → 409
- ❌ Non-admin → 403

```bash
curl -X POST http://localhost:8080/api/admin/categories \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vitamins & Supplements",
    "slug": "vitamins-supplements",
    "iconUrl": "https://cdn.example.com/icons/vitamins.png"
  }'
```

---

### PUT /api/admin/categories/{id}
Update a category.

**Conditions to test:**
- ✅ Valid ID + payload → 200
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl -X PUT http://localhost:8080/api/admin/categories/1 \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Pain Relief", "slug": "pain-relief"}'
```

---

### PATCH /api/admin/categories/{id}/deactivate
Deactivate a category.

**Conditions to test:**
- ✅ Active category → 200
- ❌ Already inactive → 400
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl -X PATCH http://localhost:8080/api/admin/categories/1/deactivate \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/prescriptions/queue
Get pending prescriptions awaiting review.

**Conditions to test:**
- ✅ Admin token → 200 + paginated list
- ✅ `?userId=5` → filter by user
- ✅ `?page=0&size=10` → pagination
- ❌ Non-admin → 403

```bash
curl "http://localhost:8080/api/admin/prescriptions/queue?page=0&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/prescriptions
Get all prescriptions with optional filters.

**Conditions to test:**
- ✅ No params → 200 + all prescriptions
- ✅ `?status=APPROVED` → filtered
- ✅ `?userId=3` → by user
- ❌ Non-admin → 403

Valid status values: `PENDING`, `APPROVED`, `REJECTED`

```bash
curl "http://localhost:8080/api/admin/prescriptions?status=PENDING&page=0&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### PATCH /api/admin/prescriptions/{id}/review
Approve or reject a prescription.

**Conditions to test:**
- ✅ `status: APPROVED` → 200 + approved prescription
- ✅ `status: REJECTED` + remarks → 200 + rejected
- ❌ Invalid status value → 400
- ❌ Already reviewed → 400
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
# Approve
curl -X PATCH http://localhost:8080/api/admin/prescriptions/1/review \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED", "remarks": "Valid prescription"}'

# Reject
curl -X PATCH http://localhost:8080/api/admin/prescriptions/1/review \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "REJECTED", "remarks": "Prescription is expired"}'
```

---

### GET /api/admin/orders
List all orders with filters.

**Conditions to test:**
- ✅ No params → 200 + paginated orders
- ✅ `?status=PAID` → filter by status
- ✅ `?userId=3` → filter by customer
- ✅ `?page=0&size=10` → pagination
- ❌ Non-admin → 403

```bash
curl "http://localhost:8080/api/admin/orders?status=PAID&page=0&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/orders/{id}
Get a specific order (admin view, any user's order).

**Conditions to test:**
- ✅ Valid ID → 200 + full order detail
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl http://localhost:8080/api/admin/orders/1 \
  -H "Authorization: Bearer <admin_token>"
```

---

### PATCH /api/admin/orders/{id}/status
Update order status (admin workflow).

**Conditions to test:**
- ✅ Valid status transition (e.g. PAID → PACKED) → 200
- ❌ Invalid transition (e.g. DELIVERED → PAID) → 400
- ❌ Non-existent order → 404
- ❌ Non-admin → 403

```bash
curl -X PATCH http://localhost:8080/api/admin/orders/1/status \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "PACKED", "note": "Packed and ready for dispatch"}'
```

---

### POST /api/admin/orders/{id}/cancel
Cancel an order as admin.

**Conditions to test:**
- ✅ Cancellable order → 204 No Content
- ❌ Already delivered/cancelled → 400
- ❌ Non-existent order → 404
- ❌ Non-admin → 403

```bash
curl -X POST http://localhost:8080/api/admin/orders/1/cancel \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"note": "Cancelled due to stock issue"}'
```

---

### GET /api/admin/users
List all users with optional filters.

**Conditions to test:**
- ✅ No params → 200 + paginated users
- ✅ `?role=CUSTOMER` → filter by role
- ✅ `?status=ACTIVE` → filter by status
- ✅ `?q=john` → search by name/email
- ❌ Non-admin → 403

Valid role values: `CUSTOMER`, `ADMIN`
Valid status values: `ACTIVE`, `SUSPENDED`, `INACTIVE`

```bash
curl "http://localhost:8080/api/admin/users?role=CUSTOMER&status=ACTIVE&page=0&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### GET /api/admin/users/{id}
Get a user by ID.

**Conditions to test:**
- ✅ Valid ID → 200 + user profile
- ❌ Non-existent ID → 404
- ❌ Non-admin → 403

```bash
curl http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer <admin_token>"
```

---

### PATCH /api/admin/users/{id}/status
Activate or suspend a user account.

**Conditions to test:**
- ✅ Valid status change → 200 + updated user
- ❌ Invalid status value → 400
- ❌ Non-existent user → 404
- ❌ Non-admin → 403

```bash
# Suspend a user
curl -X PATCH http://localhost:8080/api/admin/users/5/status \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "SUSPENDED"}'

# Reactivate a user
curl -X PATCH http://localhost:8080/api/admin/users/5/status \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "ACTIVE"}'
```

---

### POST /api/admin/users
Create a new user (admin or customer) directly.

**Conditions to test:**
- ✅ Valid payload → 201 + user profile
- ❌ Duplicate email → 409
- ❌ Missing required fields → 400
- ❌ Non-admin → 403

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Staff Admin",
    "email": "staff@pharmacy.com",
    "mobile": "9000000001",
    "password": "Admin@1234",
    "role": "ADMIN"
  }'
```

---

## Quick Reference — HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200  | OK |
| 201  | Created |
| 204  | No Content |
| 400  | Bad Request (validation error) |
| 401  | Unauthorized (missing/invalid token) |
| 403  | Forbidden (insufficient role) |
| 404  | Not Found |
| 409  | Conflict (duplicate resource) |

---

## Typical Test Flow

```bash
# 1. Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","mobile":"9999999999","password":"Test@1234"}'

# 2. Login — copy accessToken from response
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234"}'

# 3. Browse medicines
curl "http://localhost:8080/api/catalog/medicines?inStock=true"

# 4. Add to cart
curl -X POST http://localhost:8080/api/orders/cart/items \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"batchId":1,"quantity":2}'

# 5. Start checkout
curl -X POST http://localhost:8080/api/orders/checkout/start \
  -H "Authorization: Bearer <access_token>"

# 6. Set address (use orderId from step 5)
curl -X POST http://localhost:8080/api/orders/checkout/1/address \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"inlineAddress":{"line1":"123 Street","city":"Mumbai","state":"Maharashtra","pincode":"400001"}}'

# 7. Confirm order
curl -X POST http://localhost:8080/api/orders/checkout/1/confirm \
  -H "Authorization: Bearer <access_token>"

# 8. Initiate payment
curl -X POST http://localhost:8080/api/orders/payments/initiate \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"paymentMethod":"COD"}'
```
