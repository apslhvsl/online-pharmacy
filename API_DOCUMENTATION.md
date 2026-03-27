# Pharmacy API Documentation

All requests go through the **API Gateway** at `http://localhost:8080`.

## Authentication

Most endpoints require a JWT token in the `Authorization` header:
```
Authorization: Bearer <token>
```

Roles: `CUSTOMER`, `ADMIN`

Public endpoints (no token needed): `POST /api/auth/signup`, `POST /api/auth/login`

The gateway injects `X-User-Id` and `X-User-Role` headers into downstream requests automatically — you do not send these yourself (except when calling services directly).

---

## Auth Service — `/api/auth`

### POST /api/auth/signup
Register a new user.

**Auth:** None

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Secret1@"
}
```
Password rules: min 8 chars, at least one uppercase, one digit, one special character (`@$!%*?&`).

**Response `201`:**
```json
{
  "token": "<jwt>",
  "userId": 1,
  "role": "CUSTOMER"
}
```

---

### POST /api/auth/login
Authenticate and receive a JWT.

**Auth:** None

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "Secret1@"
}
```

**Response `200`:**
```json
{
  "token": "<jwt>",
  "userId": 1,
  "role": "CUSTOMER"
}
```

---

### GET /api/auth/me
Get the profile of the currently authenticated user.

**Auth:** Bearer token (any role)

**Response `200`:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

---

## Catalog Service — `/api/catalog`

### GET /api/catalog/categories
List all medicine categories.

**Auth:** Bearer token (any role)

**Response `200`:**
```json
[
  { "id": 1, "name": "Antibiotics", "description": "..." }
]
```

---

### GET /api/catalog/medicines
List medicines with optional filtering and pagination.

**Auth:** Bearer token (any role)

**Query Params:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | No | Filter by name (partial match) |
| `categoryId` | long | No | Filter by category |
| `page` | int | No | Page number (default 0) |
| `size` | int | No | Page size (default 10) |
| `sort` | string | No | Sort field (default `name`) |

**Response `200`:** Paginated list of medicines.
```json
{
  "content": [
    {
      "id": 1,
      "name": "Amoxicillin",
      "description": "...",
      "price": 12.50,
      "stockQuantity": 100,
      "requiresPrescription": false,
      "categoryId": 1,
      "categoryName": "Antibiotics",
      "expiryDate": "2026-12-01"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "number": 0
}
```

---

### GET /api/catalog/medicines/all
Get all medicines as a flat list (no pagination).

**Auth:** Bearer token (any role)

**Response `200`:** Array of medicine objects (same shape as above).

---

### GET /api/catalog/medicines/{id}
Get a single medicine by ID.

**Auth:** Bearer token (any role)

**Path Param:** `id` — medicine ID

**Response `200`:** Single medicine object.

---

### GET /api/catalog/medicines/{id}/stock-check
Check if a medicine has enough stock.

**Auth:** Bearer token (any role)

**Path Param:** `id` — medicine ID

**Query Param:** `quantity` (int, required)

**Response `200`:**
```json
{
  "medicineId": 1,
  "requestedQuantity": 5,
  "availableQuantity": 100,
  "sufficient": true
}
```

---

### POST /api/catalog/medicines
Create a new medicine.

**Auth:** Bearer token — `ADMIN` only

**Request Body:**
```json
{
  "name": "Amoxicillin",
  "description": "Broad-spectrum antibiotic",
  "price": 12.50,
  "stockQuantity": 100,
  "requiresPrescription": false,
  "categoryId": 1,
  "expiryDate": "2026-12-01"
}
```

**Response `201`:** Created medicine object.

---

### PUT /api/catalog/medicines/{id}
Update an existing medicine.

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id` — medicine ID

**Request Body:** Same as create.

**Response `200`:** Updated medicine object.

---

### PUT /api/catalog/medicines/{id}/stock/deduct
Deduct stock quantity (used internally by order service).

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id` — medicine ID

**Query Param:** `quantity` (int, required)

**Response `204`:** No content.

---

### POST /api/catalog/prescriptions/upload
Upload a prescription image.

**Auth:** Bearer token (`CUSTOMER` or `ADMIN`)

**Content-Type:** `multipart/form-data`

**Form Field:** `file` — the image file

**Response `201`:**
```json
{
  "id": 10,
  "userId": 1,
  "originalFileName": "prescription.jpg",
  "status": "PENDING",
  "uploadedAt": "2026-03-26T10:00:00"
}
```

---

### GET /api/catalog/prescriptions/{id}
Get a prescription by ID.

**Auth:** Bearer token (any role)

**Path Param:** `id` — prescription ID

**Response `200`:** Prescription object (same shape as above).

---

### GET /api/catalog/prescriptions/{id}/status
Get just the status of a prescription.

**Auth:** Bearer token (any role)

**Path Param:** `id` — prescription ID

**Response `200`:** One of `PENDING`, `APPROVED`, `REJECTED`

---

## Order Service — `/api/orders`

### GET /api/orders/cart
Get the current user's cart.

**Auth:** Bearer token (any role)

**Response `200`:**
```json
{
  "userId": 1,
  "items": {
    "3": 2,
    "7": 1
  }
}
```
`items` is a map of `medicineId -> quantity`.

---

### POST /api/orders/cart/items
Add an item to the cart (or update quantity if already present).

**Auth:** Bearer token (any role)

**Query Params:**
| Param | Type | Required |
|-------|------|----------|
| `medicineId` | long | Yes |
| `quantity` | int | Yes |

**Response `200`:** Updated cart object.

---

### DELETE /api/orders/cart/items/{medicineId}
Remove a specific item from the cart.

**Auth:** Bearer token (any role)

**Path Param:** `medicineId`

**Response `200`:** Updated cart object.

---

### DELETE /api/orders/cart
Clear the entire cart.

**Auth:** Bearer token (any role)

**Response `204`:** No content.

---

### POST /api/orders/checkout
Place an order from the current cart.

**Auth:** Bearer token (any role)

**Request Body:**
```json
{
  "shippingAddress": "123 Main St, City",
  "prescriptionId": 10,
  "idempotencyKey": "unique-key-abc123"
}
```
- `prescriptionId` — optional, required only if cart contains prescription medicines
- `idempotencyKey` — optional, prevents duplicate orders on retry

**Response `201`:** Order object (see below).

---

### GET /api/orders
Get all orders for the authenticated user.

**Auth:** Bearer token (any role)

**Response `200`:**
```json
[
  {
    "id": 5,
    "userId": 1,
    "status": "PENDING",
    "totalAmount": 37.50,
    "prescriptionId": null,
    "shippingAddress": "123 Main St",
    "createdAt": "2026-03-26T10:00:00",
    "items": [
      {
        "medicineId": 3,
        "medicineName": "Amoxicillin",
        "quantity": 2,
        "unitPrice": 12.50,
        "subtotal": 25.00
      }
    ]
  }
]
```

---

### GET /api/orders/{id}
Get a specific order (must belong to the authenticated user).

**Auth:** Bearer token (any role)

**Path Param:** `id` — order ID

**Response `200`:** Single order object.

---

### DELETE /api/orders/{id}/cancel
Cancel an order.

**Auth:** Bearer token (any role, must own the order)

**Path Param:** `id` — order ID

**Response `200`:** Updated order object with status `CANCELLED`.

---

### POST /api/orders/{id}/pay
Process payment for an order.

**Auth:** Bearer token (any role, must own the order)

**Path Param:** `id` — order ID

**Response `200`:**
```json
{
  "id": 1,
  "orderId": 5,
  "amount": 37.50,
  "status": "SUCCESS",
  "createdAt": "2026-03-26T10:05:00"
}
```

---

### GET /api/orders/{id}/payment
Get payment details for an order.

**Auth:** Bearer token (any role)

**Path Param:** `id` — order ID

**Response `200`:** Payment object (same shape as above).

---

### PATCH /api/orders/{id}/status
Update order status (internal — used by admin service).

**Auth:** Bearer token — `ADMIN` only (called internally)

**Path Param:** `id` — order ID

**Query Param:** `status` — one of `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

**Response `200`:** Updated order object.

---

### GET /api/orders/all
Get all orders across all users (admin use).

**Auth:** Bearer token — `ADMIN` only (called internally by admin service)

**Response `200`:** Array of order objects.

---

### GET /api/orders/admin/{id}
Get any order by ID without user ownership check (admin use).

**Auth:** Bearer token — `ADMIN` only (called internally by admin service)

**Path Param:** `id` — order ID

**Response `200`:** Single order object.

---

## Admin Service — `/api/admin`

All admin endpoints require `ADMIN` role.

### GET /api/admin/medicines
List all medicines.

**Auth:** Bearer token — `ADMIN` only

**Response `200`:** Array of medicine objects.

---

### GET /api/admin/medicines/{id}
Get a medicine by ID.

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id`

**Response `200`:** Single medicine object.

---

### POST /api/admin/medicines
Create a new medicine.

**Auth:** Bearer token — `ADMIN` only

**Request Body:**
```json
{
  "name": "Ibuprofen",
  "description": "Anti-inflammatory",
  "categoryId": 2,
  "price": 8.99,
  "stockQuantity": 200,
  "requiresPrescription": false,
  "expiryDate": "2027-06-01"
}
```

**Response `201`:** Created medicine object.

---

### PUT /api/admin/medicines/{id}
Update a medicine.

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id`

**Request Body:** Same as create.

**Response `200`:** Updated medicine object.

---

### PUT /api/admin/prescriptions/{id}/status
Approve or reject a prescription.

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id` — prescription ID

**Query Param:** `status` — one of `APPROVED`, `REJECTED`

**Response `204`:** No content.

---

### GET /api/admin/orders
Get all orders.

**Auth:** Bearer token — `ADMIN` only

**Response `200`:**
```json
[
  {
    "id": 5,
    "userId": 1,
    "status": "PENDING",
    "totalAmount": 37.50,
    "createdAt": "2026-03-26T10:00:00",
    "items": [...]
  }
]
```

---

### GET /api/admin/orders/{id}
Get a specific order by ID.

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id`

**Response `200`:** Single order object.

---

### PUT /api/admin/orders/{id}/status
Update the status of an order.

**Auth:** Bearer token — `ADMIN` only

**Path Param:** `id`

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```
Valid values: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

**Response `200`:** Updated order object.

---

### GET /api/admin/dashboard
Get a summary dashboard for the admin panel.

**Auth:** Bearer token — `ADMIN` only

**Response `200`:**
```json
{
  "totalMedicines": 120,
  "lowStockCount": 5,
  "pendingOrdersCount": 12,
  "totalOrdersCount": 340,
  "lowStockMedicines": [...]
}
```

---

### GET /api/admin/reports/sales
Get a sales report.

**Auth:** Bearer token — `ADMIN` only

**Response `200`:**
```json
{
  "totalOrdersCompleted": 280,
  "totalRevenue": 14500.00,
  "topMedicines": [
    {
      "medicineId": 3,
      "medicineName": "Amoxicillin",
      "totalQuantitySold": 450,
      "totalRevenue": 5625.00
    }
  ]
}
```

---

## Error Responses

| Status | Meaning |
|--------|---------|
| `400` | Validation error / bad request |
| `401` | Missing or invalid JWT |
| `403` | Insufficient role / access denied |
| `404` | Resource not found |
| `409` | Conflict (e.g. duplicate email) |
| `500` | Internal server error |

---

## Swagger UI

When the stack is running, interactive docs are available at:
```
http://localhost:8080/swagger-ui.html
```
It aggregates docs from all four services.

