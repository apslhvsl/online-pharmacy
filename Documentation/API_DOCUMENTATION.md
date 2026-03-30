# Pharmacy Backend — API Documentation

All requests go through the **API Gateway** at `http://localhost:8080`.  
Protected endpoints require a `Bearer` token in the `Authorization` header.  
The gateway injects `X-User-Id` and `X-User-Role` headers — **never send these manually from a client**.

---

## Quick Start

```bash
# 1. Start the stack
docker-compose up -d

# 2. Sign up and capture your token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","mobile":"9876543210","password":"Pass@1234"}' \
  | jq -r '.accessToken')

# 3. Use the token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/auth/me
```

---

## Base URLs

| Service           | Direct Port | Gateway Prefix       |
|-------------------|-------------|----------------------|
| API Gateway       | 8080        | —                    |
| Auth Service      | 8081        | `/api/auth`          |
| Catalog Service   | 8082        | `/api/catalog`       |
| Order Service     | 8083        | `/api/orders`        |
| Admin Service     | 8084        | `/api/admin`         |
| Notification Svc  | 8085        | (no public routes)   |
| Eureka Dashboard  | 8761        | —                    |
| RabbitMQ UI       | 15672       | —                    |
| Zipkin UI         | 9411        | —                    |
| Swagger UI        | 8080        | `/swagger-ui.html`   |

---

## Auth Roles

| Role       | Access                                              |
|------------|-----------------------------------------------------|
| `CUSTOMER` | Public catalog reads, own orders, own prescriptions |
| `ADMIN`    | Everything including `/api/admin/**`                |

---

---

# 1. AUTH SERVICE

---

## POST /api/auth/signup

**Why it exists:** Creates a new customer account. Returns tokens immediately so the user is logged in right after registration — no separate login step needed.

**Auth:** Public

**Request body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "mobile": "9876543210",
  "password": "Pass@1234"
}
```

Password rules: min 8 chars, 1 uppercase, 1 digit, 1 special character (`@$!%*?&`).

**Response `201`:**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 86400,
  "userId": 1,
  "userRole": "CUSTOMER"
}
```

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "mobile": "9876543210",
    "password": "Pass@1234"
  }'
```

---

## POST /api/auth/login

**Why it exists:** Authenticates an existing user and issues a fresh access + refresh token pair.

**Auth:** Public

**Request body:**
```json
{
  "email": "john@example.com",
  "password": "Pass@1234"
}
```

**Response `200`:** Same shape as signup response.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Pass@1234"}'
```

---

## POST /api/auth/refresh

**Why it exists:** Access tokens expire in 24h. This endpoint issues a new access token using the long-lived refresh token (30 days), without requiring the user to log in again. The old refresh token is revoked and a new one is issued (rotation).

**Auth:** Public

**Request body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `200`:** Same shape as login response.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"550e8400-e29b-41d4-a716-446655440000"}'
```

---

## POST /api/auth/forgot-password

**Why it exists:** Initiates the password reset flow. Generates a single-use token (expires in 15 min) and sends it via email. Always returns success to prevent email enumeration attacks.

**Auth:** Public

**Request body:**
```json
{
  "email": "john@example.com"
}
```

**Response `200`:**
```json
{
  "message": "If the email exists, a reset link has been sent"
}
```

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com"}'
```

---

## POST /api/auth/reset-password

**Why it exists:** Completes the password reset. Validates the token, updates the password, marks the token as used, and invalidates all existing refresh tokens (forces re-login on all devices).

**Auth:** Public

**Request body:**
```json
{
  "token": "uuid-from-email",
  "newPassword": "NewPass@5678"
}
```

**Response `200`:**
```json
{
  "message": "Password reset successful"
}
```

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"uuid-from-email","newPassword":"NewPass@5678"}'
```

---

## POST /api/auth/logout

**Why it exists:** Revokes all refresh tokens for the user. The access token remains valid until expiry (stateless JWT), but the user can no longer silently refresh — effectively ending the session.

**Auth:** CUSTOMER or ADMIN

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

## GET /api/auth/me

**Why it exists:** Returns the authenticated user's own profile. Used by the frontend to display account info and determine role-based UI.

**Auth:** CUSTOMER or ADMIN

**Response `200`:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "mobile": "9876543210",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "createdAt": "2026-01-15T10:30:00"
}
```

**curl:**
```bash
curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## PUT /api/auth/me

**Why it exists:** Lets the user update their display name and mobile number. Email is intentionally not updatable here (would require re-verification in a real system).

**Auth:** CUSTOMER or ADMIN

**Request body:**
```json
{
  "name": "John Updated",
  "mobile": "9999999999"
}
```

**Response `200`:** Updated `UserProfileResponse`.

**curl:**
```bash
curl -s -X PUT http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Updated","mobile":"9999999999"}'
```

---

## POST /api/auth/change-password

**Why it exists:** Allows an authenticated user to change their password. Requires the current password as proof of identity. Invalidates all refresh tokens on success (forces re-login everywhere).

**Auth:** CUSTOMER or ADMIN

**Request body:**
```json
{
  "currentPassword": "Pass@1234",
  "newPassword": "NewPass@5678"
}
```

**Response `200`:**
```json
{
  "message": "Password changed successfully"
}
```

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"Pass@1234","newPassword":"NewPass@5678"}'
```

---

---

# 2. CATALOG SERVICE — Public Endpoints

---

## GET /api/catalog/medicines

**Why it exists:** The main product listing page. Supports full-text search, category filter, prescription filter, and price range. Only returns active medicines. Stock is computed live from non-expired inventory batches.

**Auth:** Public (no token required)

**Query params:**

| Param                 | Type    | Description                          |
|-----------------------|---------|--------------------------------------|
| `q`                   | string  | Search by medicine name (partial)    |
| `categoryId`          | long    | Filter by category                   |
| `requiresPrescription`| boolean | `true` = Rx only, `false` = OTC only |
| `minPrice`            | decimal | Minimum price filter                 |
| `maxPrice`            | decimal | Maximum price filter                 |
| `page`                | int     | Page number (default 0)              |
| `size`                | int     | Page size (default 10)               |
| `sort`                | string  | Sort field (default `name`)          |

**Response `200`:** Paginated list of medicines.
```json
{
  "content": [
    {
      "id": 1,
      "name": "Paracetamol 500mg",
      "categoryId": 2,
      "categoryName": "Pain Relief",
      "price": 25.00,
      "active": true,
      "requiresPrescription": false,
      "manufacturer": "Sun Pharma",
      "strength": "500mg",
      "packSize": "10 tablets",
      "description": "For mild to moderate pain",
      "imageUrl": "https://...",
      "reorderLevel": 10,
      "stock": 250,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10,
  "last": true
}
```

**curl:**
```bash
# All medicines
curl -s "http://localhost:8080/api/catalog/medicines"

# Search by name
curl -s "http://localhost:8080/api/catalog/medicines?q=paracetamol"

# OTC only, price range
curl -s "http://localhost:8080/api/catalog/medicines?requiresPrescription=false&minPrice=10&maxPrice=100"

# By category, page 2
curl -s "http://localhost:8080/api/catalog/medicines?categoryId=2&page=1&size=5"
```

---

## GET /api/catalog/medicines/{id}

**Why it exists:** Product detail page. Returns full medicine info including live stock count from inventory batches.

**Auth:** Public

**Path param:** `id` — medicine ID

**Response `200`:** Single `MedicineDto` (same shape as above).

**Errors:**
- `404` — medicine not found or inactive

**curl:**
```bash
curl -s http://localhost:8080/api/catalog/medicines/1
```

---

## GET /api/catalog/categories

**Why it exists:** Populates the category navigation/filter dropdown on the frontend. Only returns active categories with a count of their medicines.

**Auth:** Public

**Response `200`:**
```json
[
  {
    "id": 1,
    "name": "Antibiotics",
    "slug": "antibiotics",
    "active": true,
    "medicineCount": 12
  },
  {
    "id": 2,
    "name": "Pain Relief",
    "slug": "pain-relief",
    "active": true,
    "medicineCount": 8
  }
]
```

**curl:**
```bash
curl -s http://localhost:8080/api/catalog/categories
```

---

## GET /api/catalog/categories/{id}

**Why it exists:** Fetches a single category by ID. Used when the frontend needs to display the category name for a selected filter.

**Auth:** Public

**curl:**
```bash
curl -s http://localhost:8080/api/catalog/categories/1
```

---

# 3. CATALOG SERVICE — Prescription Endpoints

---

## POST /api/catalog/prescriptions/upload

**Why it exists:** Customers upload a doctor's prescription image/PDF before they can purchase prescription-required medicines. The file is stored server-side and enters a PENDING review queue for admin approval.

**Auth:** CUSTOMER or ADMIN

**Request:** `multipart/form-data`

| Field  | Type | Description                          |
|--------|------|--------------------------------------|
| `file` | file | PDF, JPG, or PNG. Max size: 5MB      |

**Response `201`:**
```json
{
  "id": 10,
  "userId": 1,
  "fileName": "prescription.pdf",
  "filePath": "uploads/prescriptions/1/uuid_prescription.pdf",
  "status": "PENDING",
  "uploadedAt": "2026-03-28T10:00:00",
  "reviewedAt": null,
  "reviewedBy": null,
  "validTill": null,
  "remarks": null
}
```

**Errors:**
- `415` — file type not allowed (only PDF, JPG, PNG)
- `409` — file exceeds 5MB

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/catalog/prescriptions/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/prescription.pdf"
```

---

## GET /api/catalog/prescriptions

**Why it exists:** Lets a customer see all their uploaded prescriptions and their approval status. Used to pick an approved prescription during checkout.

**Auth:** CUSTOMER or ADMIN

**Response `200`:** Array of `PrescriptionDto`.

**curl:**
```bash
curl -s http://localhost:8080/api/catalog/prescriptions \
  -H "Authorization: Bearer $TOKEN"
```

---

## GET /api/catalog/prescriptions/{id}

**Why it exists:** Fetches a single prescription. Ownership is enforced — a customer can only view their own prescriptions.

**Auth:** CUSTOMER or ADMIN

**Errors:**
- `403` — prescription belongs to a different user
- `404` — not found

**curl:**
```bash
curl -s http://localhost:8080/api/catalog/prescriptions/10 \
  -H "Authorization: Bearer $TOKEN"
```

---

## GET /api/catalog/prescriptions/{id}/file

**Why it exists:** Downloads the actual prescription file (PDF/image). Used by the customer to view what they uploaded, and by the admin to review it. Ownership is enforced for customers.

**Auth:** CUSTOMER or ADMIN

**Response `200`:** Binary file stream with correct `Content-Type` header.

**curl:**
```bash
# View in browser / save to disk
curl -s http://localhost:8080/api/catalog/prescriptions/10/file \
  -H "Authorization: Bearer $TOKEN" \
  --output prescription.pdf
```

---

---

# 4. ORDER SERVICE — Cart

Cart items are keyed by `batchId` (not `medicineId`) because inventory is tracked at the batch level. When you add an item, the service validates stock against that specific batch and snapshots the medicine name and price.

---

## GET /api/orders/cart

**Why it exists:** Returns the current user's cart with live line totals, subtotal, 5% GST, and a flag indicating whether any item requires a prescription. The frontend uses this to render the cart page and decide whether to prompt for a prescription.

**Auth:** CUSTOMER or ADMIN

**Response `200`:**
```json
{
  "userId": 1,
  "items": [
    {
      "batchId": 5,
      "medicineId": 1,
      "medicineName": "Paracetamol 500mg",
      "unitPrice": 25.00,
      "quantity": 3,
      "requiresPrescription": false,
      "lineTotal": 75.00
    }
  ],
  "subTotal": 75.00,
  "taxAmount": 3.75,
  "total": 78.75,
  "requiresPrescription": false
}
```

**curl:**
```bash
curl -s http://localhost:8080/api/orders/cart \
  -H "Authorization: Bearer $TOKEN"
```

---

## POST /api/orders/cart/items

**Why it exists:** Adds a medicine batch to the cart. Validates stock availability in real-time via the catalog service before adding. If the item already exists in the cart, the quantity is incremented.

**Auth:** CUSTOMER or ADMIN

**Request body:**
```json
{
  "batchId": 5,
  "quantity": 2
}
```

**Response `200`:** Updated `CartDto`.

**Errors:**
- `409` — insufficient stock
- `400` — medicine is inactive

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"batchId":5,"quantity":2}'
```

---

## PUT /api/orders/cart/items/{batchId}

**Why it exists:** Updates the quantity of a specific item in the cart. Sending `quantity: 0` removes the item — this avoids needing a separate "remove" endpoint for quantity updates.

**Auth:** CUSTOMER or ADMIN

**Path param:** `batchId`

**Request body:**
```json
{
  "quantity": 5
}
```

**Response `200`:** Updated `CartDto`.

**curl:**
```bash
# Update quantity
curl -s -X PUT http://localhost:8080/api/orders/cart/items/5 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity":5}'

# Remove item (quantity = 0)
curl -s -X PUT http://localhost:8080/api/orders/cart/items/5 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity":0}'
```

---

## DELETE /api/orders/cart/items/{batchId}

**Why it exists:** Explicitly removes a single item from the cart. Cleaner than PUT with quantity 0 when the intent is clearly deletion.

**Auth:** CUSTOMER or ADMIN

**Response `200`:** Updated `CartDto`.

**curl:**
```bash
curl -s -X DELETE http://localhost:8080/api/orders/cart/items/5 \
  -H "Authorization: Bearer $TOKEN"
```

---

## DELETE /api/orders/cart

**Why it exists:** Clears the entire cart. Used when the user wants to start fresh, or automatically called by the system after a successful order confirmation.

**Auth:** CUSTOMER or ADMIN

**Response `204`:** No content.

**curl:**
```bash
curl -s -X DELETE http://localhost:8080/api/orders/cart \
  -H "Authorization: Bearer $TOKEN"
```

---

# 5. ORDER SERVICE — Checkout (4-Step Flow)

Checkout is a multi-step process. Each step returns the current order state. Steps must be followed in order.

```
Step 1: POST /checkout/start              → creates order, returns orderId
Step 2: POST /checkout/{orderId}/address  → attach delivery address
Step 3: POST /checkout/{orderId}/prescription-link  → (only if cart has Rx items)
Step 4: POST /checkout/{orderId}/confirm  → validates stock, deducts inventory, moves to PAYMENT_PENDING
```

---

## POST /api/orders/checkout/start

**Why it exists:** Initiates checkout by creating a draft order from the current cart. Returns the `orderId` needed for subsequent steps, and tells the frontend whether a prescription is required.

**Auth:** CUSTOMER or ADMIN

**Response `201`:**
```json
{
  "orderId": 42,
  "status": "CHECKOUT_STARTED",
  "requiresPrescription": false,
  "prescriptionStatus": null
}
```

**Errors:**
- `409` — cart is empty

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/checkout/start \
  -H "Authorization: Bearer $TOKEN"
```

---

## POST /api/orders/checkout/{orderId}/address

**Why it exists:** Attaches a delivery address to the order. You can either reference an existing saved address by ID, or provide a new inline address which gets saved automatically.

**Auth:** CUSTOMER or ADMIN

**Path param:** `orderId`

**Request body — use saved address:**
```json
{
  "addressId": 3
}
```

**Request body — inline new address:**
```json
{
  "inlineAddress": {
    "label": "Home",
    "line1": "123 MG Road",
    "line2": "Apt 4B",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "isDefault": false
  }
}
```

**Response `200`:** Updated `OrderDto`.

**curl:**
```bash
# Use saved address
curl -s -X POST http://localhost:8080/api/orders/checkout/42/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"addressId":3}'

# Inline address
curl -s -X POST http://localhost:8080/api/orders/checkout/42/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inlineAddress": {
      "label":"Home","line1":"123 MG Road","city":"Bangalore",
      "state":"Karnataka","pincode":"560001","isDefault":false
    }
  }'
```

---

## POST /api/orders/checkout/{orderId}/prescription-link

**Why it exists:** Links an approved prescription to the order. Required before confirming if any cart item has `requiresPrescription: true`. The system validates that the prescription is APPROVED, not expired, and belongs to the requesting user.

**Auth:** CUSTOMER or ADMIN

**Path param:** `orderId`

**Query param:** `prescriptionId` (long)

**Response `200`:** Updated `OrderDto` with `status: PRESCRIPTION_APPROVED`.

**Errors:**
- `409` — prescription is not APPROVED
- `409` — prescription has expired
- `409` — prescription belongs to a different user

**curl:**
```bash
curl -s -X POST "http://localhost:8080/api/orders/checkout/42/prescription-link?prescriptionId=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## POST /api/orders/checkout/{orderId}/confirm

**Why it exists:** The critical final step. Validates stock for every item in the cart, snapshots prices into order items, calculates totals (5% tax, free delivery above ₹500), deducts stock from inventory batches (FEFO order), clears the cart, and moves the order to `PAYMENT_PENDING`.

**Auth:** CUSTOMER or ADMIN

**Path param:** `orderId`

**Response `200`:** Fully populated `OrderDto`:
```json
{
  "id": 42,
  "orderNumber": "RX-2026-00042",
  "userId": 1,
  "addressId": 3,
  "status": "PAYMENT_PENDING",
  "prescriptionId": null,
  "subtotal": 500.00,
  "taxAmount": 25.00,
  "deliveryCharge": 0.00,
  "totalAmount": 525.00,
  "notes": null,
  "createdAt": "2026-03-28T10:00:00",
  "updatedAt": "2026-03-28T10:05:00",
  "items": [
    {
      "batchId": 5,
      "medicineId": 1,
      "medicineName": "Paracetamol 500mg",
      "unitPrice": 25.00,
      "quantity": 20,
      "lineTotal": 500.00
    }
  ]
}
```

**Errors:**
- `409` — insufficient stock for one or more items
- `409` — prescription required but not linked
- `409` — prescription not approved or expired

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/checkout/42/confirm \
  -H "Authorization: Bearer $TOKEN"
```

---

---

# 6. ORDER SERVICE — Orders & Payments

---

## GET /api/orders

**Why it exists:** Returns the authenticated user's order history with optional status filter and pagination. The customer's "My Orders" page.

**Auth:** CUSTOMER or ADMIN

**Query params:**

| Param    | Type   | Description                                                                 |
|----------|--------|-----------------------------------------------------------------------------|
| `status` | string | Filter by status: `DRAFT`, `PAYMENT_PENDING`, `PAID`, `PACKED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CUSTOMER_CANCELLED`, etc. |
| `page`   | int    | Default 0                                                                   |
| `size`   | int    | Default 10                                                                  |

**Response `200`:** Paginated list of `OrderDto`.

**curl:**
```bash
# All orders
curl -s http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"

# Only delivered orders
curl -s "http://localhost:8080/api/orders?status=DELIVERED" \
  -H "Authorization: Bearer $TOKEN"
```

---

## GET /api/orders/{id}

**Why it exists:** Order detail page. Returns full order with all items and totals. Ownership is enforced — a customer cannot view another user's order.

**Auth:** CUSTOMER or ADMIN

**Errors:**
- `404` — order not found or belongs to a different user

**curl:**
```bash
curl -s http://localhost:8080/api/orders/42 \
  -H "Authorization: Bearer $TOKEN"
```

---

## PATCH /api/orders/{id}/cancel

**Why it exists:** Allows a customer to cancel their own order. The state machine enforces that only orders in cancellable states (`DRAFT`, `CHECKOUT_STARTED`, `PRESCRIPTION_APPROVED`, `PAYMENT_PENDING`) can be cancelled. Post-payment cancellation is not allowed from this endpoint.

**Auth:** CUSTOMER or ADMIN

**Request body:**
```json
{
  "reason": "Changed my mind"
}
```

**Response `200`:** Updated `OrderDto` with `status: CUSTOMER_CANCELLED`.

**Errors:**
- `400` — invalid state transition (e.g., trying to cancel a DELIVERED order)

**curl:**
```bash
curl -s -X PATCH http://localhost:8080/api/orders/42/cancel \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Changed my mind"}'
```

---

## POST /api/orders/{id}/return

**Why it exists:** Initiates a return request for a delivered order. Only allowed within 7 days of delivery. Moves the order to `RETURN_REQUESTED` for admin review.

**Auth:** CUSTOMER or ADMIN

**Request body:**
```json
{
  "reason": "Wrong medicine delivered"
}
```

**Response `200`:** Updated `OrderDto` with `status: RETURN_REQUESTED`.

**Errors:**
- `409` — order is not in DELIVERED status
- `409` — 7-day return window has passed

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/42/return \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Wrong medicine delivered"}'
```

---

## POST /api/orders/{id}/reorder

**Why it exists:** Convenience endpoint that re-adds all items from a previous order back into the cart. Out-of-stock items are silently skipped. Saves the customer from manually finding and adding each medicine again.

**Auth:** CUSTOMER or ADMIN

**Response `200`:** Updated `CartDto` with items added.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/42/reorder \
  -H "Authorization: Bearer $TOKEN"
```

---

## POST /api/orders/payments/initiate

**Why it exists:** Initiates payment for an order in `PAYMENT_PENDING` state. For COD orders, payment is confirmed immediately and the order moves to `PAID`. For PREPAID/WALLET, a gateway transaction reference is created (actual gateway integration is a future enhancement).

**Auth:** CUSTOMER or ADMIN

**Payment methods:** `COD`, `PREPAID`, `WALLET`

**Request body:**
```json
{
  "orderId": 42,
  "paymentMethod": "COD"
}
```

**Response `200`:**
```json
{
  "id": 1,
  "orderId": 42,
  "paymentMethod": "COD",
  "status": "PAID",
  "amount": 525.00,
  "gatewayTxnRef": "uuid-ref",
  "paidAt": "2026-03-28T10:10:00",
  "refundedAt": null,
  "createdAt": "2026-03-28T10:10:00"
}
```

**Errors:**
- `409` — order is not in PAYMENT_PENDING state

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/payments/initiate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"orderId":42,"paymentMethod":"COD"}'
```

---

## GET /api/orders/payments/{orderId}

**Why it exists:** Fetches payment details for a specific order. Used on the order detail page to show payment status and transaction reference. Ownership is enforced.

**Auth:** CUSTOMER or ADMIN

**curl:**
```bash
curl -s http://localhost:8080/api/orders/payments/42 \
  -H "Authorization: Bearer $TOKEN"
```

---

# 7. ORDER SERVICE — Addresses

---

## GET /api/orders/addresses

**Why it exists:** Returns all saved delivery addresses for the user. Used to populate the address selection dropdown during checkout.

**Auth:** CUSTOMER or ADMIN

**Response `200`:**
```json
[
  {
    "id": 3,
    "userId": 1,
    "label": "Home",
    "line1": "123 MG Road",
    "line2": "Apt 4B",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "isDefault": true
  }
]
```

**curl:**
```bash
curl -s http://localhost:8080/api/orders/addresses \
  -H "Authorization: Bearer $TOKEN"
```

---

## POST /api/orders/addresses

**Why it exists:** Saves a new delivery address. If `isDefault: true`, all other addresses for this user are automatically unset as default.

**Auth:** CUSTOMER or ADMIN

**Request body:**
```json
{
  "label": "Office",
  "line1": "456 Brigade Road",
  "line2": "Floor 3",
  "city": "Bangalore",
  "state": "Karnataka",
  "pincode": "560025",
  "isDefault": false
}
```

**Response `201`:** Created `AddressDto`.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/orders/addresses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "label":"Office","line1":"456 Brigade Road","city":"Bangalore",
    "state":"Karnataka","pincode":"560025","isDefault":false
  }'
```

---

## PUT /api/orders/addresses/{id}

**Why it exists:** Updates an existing address. Ownership is enforced.

**Auth:** CUSTOMER or ADMIN

**curl:**
```bash
curl -s -X PUT http://localhost:8080/api/orders/addresses/3 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"line1":"789 New Road","city":"Bangalore","state":"Karnataka","pincode":"560001"}'
```

---

## DELETE /api/orders/addresses/{id}

**Why it exists:** Removes a saved address. Ownership is enforced.

**Auth:** CUSTOMER or ADMIN

**Response `204`:** No content.

**curl:**
```bash
curl -s -X DELETE http://localhost:8080/api/orders/addresses/3 \
  -H "Authorization: Bearer $TOKEN"
```

---

---

# 8. ADMIN SERVICE

All `/api/admin/**` endpoints require `ADMIN` role. The admin service is a pure aggregation layer — it has no database of its own. Every call proxies to the appropriate downstream service via Feign.

Set your admin token:
```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pharmacy.com","password":"Admin@1234"}' \
  | jq -r '.accessToken')
```

---

## GET /api/admin/dashboard

**Why it exists:** Single endpoint that aggregates key metrics from multiple services: total orders, today's revenue, pending prescription count, low-stock medicine count, and expiring-soon count. The admin home screen.

**Auth:** ADMIN

**Response `200`:**
```json
{
  "totalOrders": 1250,
  "todayRevenue": 45000.00,
  "pendingPrescriptions": 8,
  "lowStockCount": 3,
  "expiringCount": 5,
  "recentOrders": [...]
}
```

**curl:**
```bash
curl -s http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/reports/sales

**Why it exists:** Sales report showing total completed orders, total revenue, and a ranked list of top-selling medicines by quantity. Used for business analytics and restocking decisions.

**Auth:** ADMIN

**Response `200`:**
```json
{
  "totalOrdersCompleted": 980,
  "totalRevenue": 350000.00,
  "topMedicines": [
    {
      "medicineId": 1,
      "medicineName": "Paracetamol 500mg",
      "totalQuantitySold": 5000,
      "totalRevenue": 125000.00
    }
  ]
}
```

**curl:**
```bash
curl -s http://localhost:8080/api/admin/reports/sales \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/medicines

**Why it exists:** Admin medicine list — unlike the public endpoint, this includes inactive medicines so admins can see and reactivate them. Supports the same filters as the public endpoint.

**Auth:** ADMIN

**Query params:** `q`, `categoryId`, `requiresPrescription`, `inStock`, `page`, `size`

**curl:**
```bash
# All medicines including inactive
curl -s "http://localhost:8080/api/admin/medicines?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Low stock only
curl -s "http://localhost:8080/api/admin/medicines?inStock=false" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/medicines/{id}

**Why it exists:** Admin view of a single medicine including inactive ones.

**Auth:** ADMIN

**curl:**
```bash
curl -s http://localhost:8080/api/admin/medicines/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## POST /api/admin/medicines

**Why it exists:** Creates a new medicine in the catalog. Only admins can add medicines.

**Auth:** ADMIN

**Request body:**
```json
{
  "name": "Amoxicillin 500mg",
  "categoryId": 1,
  "price": 85.00,
  "requiresPrescription": true,
  "manufacturer": "Cipla",
  "strength": "500mg",
  "packSize": "10 capsules",
  "description": "Broad-spectrum antibiotic",
  "imageUrl": "https://cdn.example.com/amoxicillin.jpg",
  "reorderLevel": 20
}
```

**Response `201`:** Created `MedicineDto`.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/admin/medicines \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Amoxicillin 500mg","categoryId":1,"price":85.00,
    "requiresPrescription":true,"manufacturer":"Cipla",
    "strength":"500mg","packSize":"10 capsules","reorderLevel":20
  }'
```

---

## PUT /api/admin/medicines/{id}

**Why it exists:** Updates medicine details. All fields are optional — only provided fields are updated (partial update behavior).

**Auth:** ADMIN

**curl:**
```bash
curl -s -X PUT http://localhost:8080/api/admin/medicines/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"price":90.00,"description":"Updated description"}'
```

---

## PATCH /api/admin/medicines/{id}/deactivate

**Why it exists:** Soft-deletes a medicine by setting `active: false`. It disappears from the public catalog but historical order data is preserved. Hard delete is intentionally not supported.

**Auth:** ADMIN

**Response `200`:** Updated `MedicineDto` with `active: false`.

**curl:**
```bash
curl -s -X PATCH http://localhost:8080/api/admin/medicines/1/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/medicines/low-stock

**Why it exists:** Lists medicines whose total available (non-expired) stock is below the threshold. Default threshold is 10 units. Used for restocking alerts.

**Auth:** ADMIN

**Query params:** `stockLessThan` (int, default 10)

**curl:**
```bash
# Default threshold (10)
curl -s http://localhost:8080/api/admin/medicines/low-stock \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Custom threshold
curl -s "http://localhost:8080/api/admin/medicines/low-stock?stockLessThan=50" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/medicines/expiring

**Why it exists:** Lists medicines that have batches expiring within the next N days. Default is 90 days. Critical for a pharmacy to avoid dispensing expired medicines.

**Auth:** ADMIN

**Query params:** `expiryBefore` (date string, optional), `days` (int, default 90)

**curl:**
```bash
# Expiring in next 90 days
curl -s http://localhost:8080/api/admin/medicines/expiring \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Expiring in next 30 days
curl -s "http://localhost:8080/api/admin/medicines/expiring?days=30" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## PATCH /api/admin/batches/{batchId}/stock

**Why it exists:** Manual stock adjustment for a specific inventory batch. Positive `adjustment` adds stock (new delivery), negative deducts (damaged/expired removal). Every adjustment is written to the `InventoryAudit` table with a reason and the admin's ID.

**Auth:** ADMIN

**Path param:** `batchId`

**Request body:**
```json
{
  "adjustment": 100,
  "reason": "New stock received from supplier"
}
```

For deduction:
```json
{
  "adjustment": -10,
  "reason": "Damaged units removed"
}
```

**Response `204`:** No content.

**Errors:**
- `409` — adjustment would bring stock below zero

**curl:**
```bash
# Add stock
curl -s -X PATCH http://localhost:8080/api/admin/batches/5/stock \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":100,"reason":"New stock received"}'

# Remove damaged units
curl -s -X PATCH http://localhost:8080/api/admin/batches/5/stock \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-10,"reason":"Damaged units removed"}'
```

---

---

## GET /api/admin/categories

**Why it exists:** Admin view of all categories including inactive ones.

**Auth:** ADMIN

**curl:**
```bash
curl -s http://localhost:8080/api/admin/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/categories/{id}

**Auth:** ADMIN

**curl:**
```bash
curl -s http://localhost:8080/api/admin/categories/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## POST /api/admin/categories

**Why it exists:** Creates a new medicine category. The `slug` is used for URL-friendly category identifiers (e.g., `pain-relief`).

**Auth:** ADMIN

**Request body:**
```json
{
  "name": "Vitamins & Supplements",
  "slug": "vitamins-supplements"
}
```

**Response `201`:** Created `CategoryDto`.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/admin/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Vitamins & Supplements","slug":"vitamins-supplements"}'
```

---

## PUT /api/admin/categories/{id}

**Why it exists:** Updates category name or slug.

**Auth:** ADMIN

**curl:**
```bash
curl -s -X PUT http://localhost:8080/api/admin/categories/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Antibiotics & Antivirals","slug":"antibiotics-antivirals"}'
```

---

## PATCH /api/admin/categories/{id}/deactivate

**Why it exists:** Soft-deletes a category. Cannot deactivate a category that still has medicines assigned to it.

**Auth:** ADMIN

**Errors:**
- `409` — category has existing medicines

**curl:**
```bash
curl -s -X PATCH http://localhost:8080/api/admin/categories/1/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/prescriptions/queue

**Why it exists:** The admin's prescription review queue — shows all PENDING prescriptions waiting for approval. Admins work through this queue to approve or reject prescriptions before customers can use them for checkout.

**Auth:** ADMIN

**Query params:** `userId` (filter by user), `page`, `size`

**Response `200`:** Paginated list of `PrescriptionDto` with `status: PENDING`.

**curl:**
```bash
curl -s "http://localhost:8080/api/admin/prescriptions/queue?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## PATCH /api/admin/prescriptions/{id}/review

**Why it exists:** Approves or rejects a prescription. On approval, the admin can optionally set remarks. The customer is notified via email (when notification service is wired). Once approved, the prescription can be linked to a checkout.

**Auth:** ADMIN

**Request body — approve:**
```json
{
  "status": "APPROVED",
  "remarks": "Valid prescription from licensed doctor"
}
```

**Request body — reject:**
```json
{
  "status": "REJECTED",
  "remarks": "Prescription is expired or illegible"
}
```

**Response `200`:** Updated `PrescriptionDto`.

**Errors:**
- `400` — status must be APPROVED or REJECTED (cannot set back to PENDING)

**curl:**
```bash
# Approve
curl -s -X PATCH http://localhost:8080/api/admin/prescriptions/10/review \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"APPROVED","remarks":"Valid prescription"}'

# Reject
curl -s -X PATCH http://localhost:8080/api/admin/prescriptions/10/review \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"REJECTED","remarks":"Illegible document"}'
```

---

## GET /api/admin/prescriptions

**Why it exists:** Full prescription list with filters. Used for audit and compliance — admins can search by status, user, or date range.

**Auth:** ADMIN

**Query params:** `status` (`PENDING`/`APPROVED`/`REJECTED`), `userId`, `page`, `size`

**curl:**
```bash
# All approved prescriptions
curl -s "http://localhost:8080/api/admin/prescriptions?status=APPROVED" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# All prescriptions for a specific user
curl -s "http://localhost:8080/api/admin/prescriptions?userId=1" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/orders

**Why it exists:** Full order list across all users with status and user filters. The admin's order management screen.

**Auth:** ADMIN

**Query params:** `status`, `userId`, `page`, `size`

**curl:**
```bash
# All orders
curl -s "http://localhost:8080/api/admin/orders?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Paid orders only
curl -s "http://localhost:8080/api/admin/orders?status=PAID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/orders/{id}

**Why it exists:** Full order detail for any order, regardless of which user placed it.

**Auth:** ADMIN

**curl:**
```bash
curl -s http://localhost:8080/api/admin/orders/42 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## PATCH /api/admin/orders/{id}/status

**Why it exists:** Moves an order through the fulfillment pipeline. The state machine enforces valid transitions. Used by warehouse staff to mark orders as PACKED, OUT_FOR_DELIVERY, DELIVERED, etc.

**Auth:** ADMIN

**Valid status values:** `PACKED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `REFUND_INITIATED`, `REFUND_COMPLETED`

**Request body:**
```json
{
  "status": "PACKED",
  "note": "Packed and ready for dispatch"
}
```

**Response `200`:** Updated `OrderDto`.

**Errors:**
- `400` — invalid state transition

**curl:**
```bash
# Mark as packed
curl -s -X PATCH http://localhost:8080/api/admin/orders/42/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"PACKED","note":"Packed and ready for dispatch"}'

# Mark as out for delivery
curl -s -X PATCH http://localhost:8080/api/admin/orders/42/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"OUT_FOR_DELIVERY","note":"Dispatched via courier"}'

# Mark as delivered
curl -s -X PATCH http://localhost:8080/api/admin/orders/42/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"DELIVERED","note":"Delivered successfully"}'
```

---

## PATCH /api/admin/orders/{id}/cancel

**Why it exists:** Admin-side cancellation. Can cancel orders even after payment (unlike customer cancellation). Moves to `ADMIN_CANCELLED` which then allows `REFUND_INITIATED`.

**Auth:** ADMIN

**Request body:**
```json
{
  "status": "ADMIN_CANCELLED",
  "note": "Out of stock — unable to fulfil"
}
```

**Response `200`:** Updated `OrderDto`.

**curl:**
```bash
curl -s -X PATCH http://localhost:8080/api/admin/orders/42/cancel \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"note":"Out of stock — unable to fulfil"}'
```

---

## GET /api/admin/users

**Why it exists:** Lists all users with role and status filters. Used for user management — finding customers, checking account status, etc.

**Auth:** ADMIN

**Query params:** `role` (`CUSTOMER`/`ADMIN`), `status` (`ACTIVE`/`INACTIVE`/`SUSPENDED`), `q` (search by name/email), `page`, `size`

**curl:**
```bash
# All users
curl -s "http://localhost:8080/api/admin/users?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Suspended customers only
curl -s "http://localhost:8080/api/admin/users?role=CUSTOMER&status=SUSPENDED" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Search by name
curl -s "http://localhost:8080/api/admin/users?q=john" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## GET /api/admin/users/{id}

**Why it exists:** Fetches a single user's profile. Used when an admin needs to investigate a specific account.

**Auth:** ADMIN

**curl:**
```bash
curl -s http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## PATCH /api/admin/users/{id}/status

**Why it exists:** Activates, deactivates, or suspends a user account. A suspended user cannot log in. Used for fraud prevention or account management.

**Auth:** ADMIN

**Status values:** `ACTIVE`, `INACTIVE`, `SUSPENDED`

**Request body:**
```json
{
  "status": "SUSPENDED"
}
```

**Response `200`:** Updated `UserProfileResponse`.

**curl:**
```bash
# Suspend a user
curl -s -X PATCH http://localhost:8080/api/admin/users/5/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"SUSPENDED"}'

# Reactivate
curl -s -X PATCH http://localhost:8080/api/admin/users/5/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"ACTIVE"}'
```

---

## POST /api/admin/users

**Why it exists:** Admin-created accounts. Used to create other admin accounts or pre-register users. Unlike self-signup, the admin can assign any role.

**Auth:** ADMIN

**Request body:**
```json
{
  "name": "Warehouse Admin",
  "email": "warehouse@pharmacy.com",
  "mobile": "9000000001",
  "password": "Admin@5678",
  "role": "ADMIN"
}
```

**Response `201`:** Created `UserProfileResponse`.

**curl:**
```bash
curl -s -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Warehouse Admin","email":"warehouse@pharmacy.com",
    "mobile":"9000000001","password":"Admin@5678","role":"ADMIN"
  }'
```

---

---

# 9. Complete End-to-End Flow

This walks through the full customer journey from registration to a delivered order.

```bash
BASE="http://localhost:8080"

# ── Step 1: Register ──────────────────────────────────────────────────
SIGNUP=$(curl -s -X POST $BASE/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","mobile":"9123456789","password":"Pass@1234"}')
TOKEN=$(echo $SIGNUP | jq -r '.accessToken')
echo "Token: $TOKEN"

# ── Step 2: Browse medicines ──────────────────────────────────────────
curl -s "$BASE/api/catalog/medicines?q=paracetamol" | jq '.content[0]'
# Note the medicine id and batchId from the response

# ── Step 3: Add to cart ───────────────────────────────────────────────
curl -s -X POST $BASE/api/orders/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"batchId":5,"quantity":2}'

# ── Step 4: Save an address ───────────────────────────────────────────
ADDRESS=$(curl -s -X POST $BASE/api/orders/addresses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"label":"Home","line1":"123 MG Road","city":"Bangalore","state":"Karnataka","pincode":"560001","isDefault":true}')
ADDRESS_ID=$(echo $ADDRESS | jq -r '.id')

# ── Step 5: Start checkout ────────────────────────────────────────────
SESSION=$(curl -s -X POST $BASE/api/orders/checkout/start \
  -H "Authorization: Bearer $TOKEN")
ORDER_ID=$(echo $SESSION | jq -r '.orderId')
echo "Order ID: $ORDER_ID"

# ── Step 6: Set address ───────────────────────────────────────────────
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"addressId\":$ADDRESS_ID}"

# ── Step 7: Confirm order ─────────────────────────────────────────────
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/confirm \
  -H "Authorization: Bearer $TOKEN"

# ── Step 8: Pay (COD) ─────────────────────────────────────────────────
curl -s -X POST $BASE/api/orders/payments/initiate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"paymentMethod\":\"COD\"}"

# ── Step 9: Check order status ────────────────────────────────────────
curl -s $BASE/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.status'
# → "PAID"
```

---

# 10. Prescription-Required Medicine Flow

```bash
# ── Step 1: Upload prescription ───────────────────────────────────────
RX=$(curl -s -X POST $BASE/api/catalog/prescriptions/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/prescription.pdf")
RX_ID=$(echo $RX | jq -r '.id')
echo "Prescription ID: $RX_ID, Status: $(echo $RX | jq -r '.status')"
# → Status: PENDING

# ── Step 2: Admin approves prescription ──────────────────────────────
curl -s -X PATCH $BASE/api/admin/prescriptions/$RX_ID/review \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"APPROVED","remarks":"Valid prescription"}'

# ── Step 3: Add Rx medicine to cart ──────────────────────────────────
curl -s -X POST $BASE/api/orders/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"batchId":12,"quantity":1}'
# Cart will show requiresPrescription: true

# ── Step 4: Start checkout ────────────────────────────────────────────
SESSION=$(curl -s -X POST $BASE/api/orders/checkout/start \
  -H "Authorization: Bearer $TOKEN")
ORDER_ID=$(echo $SESSION | jq -r '.orderId')
# requiresPrescription: true in response

# ── Step 5: Set address ───────────────────────────────────────────────
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"addressId":3}'

# ── Step 6: Link prescription ─────────────────────────────────────────
curl -s -X POST "$BASE/api/orders/checkout/$ORDER_ID/prescription-link?prescriptionId=$RX_ID" \
  -H "Authorization: Bearer $TOKEN"

# ── Step 7: Confirm ───────────────────────────────────────────────────
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/confirm \
  -H "Authorization: Bearer $TOKEN"
```

---

# 11. Error Response Format

All services return errors in a consistent format:

```json
{
  "timestamp": "2026-03-28T10:00:00",
  "status": 404,
  "error": "Medicine not found: 999"
}
```

| HTTP Status | Meaning                                      |
|-------------|----------------------------------------------|
| `400`       | Validation error or invalid state transition |
| `401`       | Missing or expired JWT token                 |
| `403`       | Authenticated but insufficient role          |
| `404`       | Resource not found                           |
| `409`       | Business rule conflict (stock, state, etc.)  |
| `415`       | Unsupported file type (prescription upload)  |
| `500`       | Unexpected server error                      |

---

# 12. Order Status Reference

```
DRAFT
  └─► CHECKOUT_STARTED
        ├─► PRESCRIPTION_PENDING
        │     ├─► PRESCRIPTION_APPROVED ─► PAYMENT_PENDING
        │     └─► PRESCRIPTION_REJECTED (terminal)
        └─► PAYMENT_PENDING
              ├─► PAID
              │     └─► PACKED
              │           └─► OUT_FOR_DELIVERY
              │                 └─► DELIVERED
              │                       └─► RETURN_REQUESTED
              │                             └─► REFUND_INITIATED
              │                                   └─► REFUND_COMPLETED (terminal)
              └─► PAYMENT_FAILED (terminal)

CUSTOMER_CANCELLED (terminal — from DRAFT, CHECKOUT_STARTED, PRESCRIPTION_APPROVED, PAYMENT_PENDING)
ADMIN_CANCELLED    (from PAID, PACKED) ─► REFUND_INITIATED ─► REFUND_COMPLETED
```

---

# 13. Swagger UI

The API Gateway aggregates Swagger docs from all services into a single UI:

```
http://localhost:8080/swagger-ui.html
```

Individual service docs are available at:
- Auth:    `http://localhost:8080/auth-service/v3/api-docs`
- Catalog: `http://localhost:8080/catalog-service/v3/api-docs`
- Orders:  `http://localhost:8080/order-service/v3/api-docs`
- Admin:   `http://localhost:8080/admin-service/v3/api-docs`
