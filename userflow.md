# User Flow: Sign Up to Checkout

All requests go through the API Gateway at `http://localhost:8080`.

---

## Step 1 — Sign Up

**POST** `/api/auth/signup`

No token needed.

```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Secret1@"
}
```

Response `201`:
```json
{
  "token": "<jwt>",
  "userId": 1,
  "role": "CUSTOMER"
}
```

> Save the `token` — you'll attach it as `Authorization: Bearer <token>` on every request from here on.

---

## Step 2 — Browse Categories (optional)

**GET** `/api/catalog/categories`

```http
GET http://localhost:8080/api/catalog/categories
Authorization: Bearer <token>
```

Returns a list of medicine categories to help the user filter what they're looking for.

---

## Step 3 — Browse Medicines

**GET** `/api/catalog/medicines`

```http
GET http://localhost:8080/api/catalog/medicines?name=amox&page=0&size=10
Authorization: Bearer <token>
```

Response includes `requiresPrescription` per item — flag this for Step 5.

---

## Step 4 — View a Medicine

**GET** `/api/catalog/medicines/{id}`

```http
GET http://localhost:8080/api/catalog/medicines/1
Authorization: Bearer <token>
```

Check `requiresPrescription`, `price`, and `stockQuantity` before adding to cart.

---

## Step 5 — Upload Prescription (if required)

Only needed if any medicine in the cart has `requiresPrescription: true`.

**POST** `/api/catalog/prescriptions/upload`

```http
POST http://localhost:8080/api/catalog/prescriptions/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <image file>
```

Response `201`:
```json
{
  "id": 10,
  "userId": 1,
  "status": "PENDING",
  "uploadedAt": "2026-03-26T10:00:00"
}
```

> Save the `id` as `prescriptionId` for checkout.

### Step 5a — Poll Prescription Status

Wait for admin approval before checking out.

**GET** `/api/catalog/prescriptions/{id}/status`

```http
GET http://localhost:8080/api/catalog/prescriptions/10/status
Authorization: Bearer <token>
```

Returns one of: `PENDING` | `APPROVED` | `REJECTED`

Only proceed to checkout once status is `APPROVED`.

---

## Step 6 — Add Items to Cart

**POST** `/api/orders/cart/items`

Repeat for each medicine the user wants.

```http
POST http://localhost:8080/api/orders/cart/items?medicineId=1&quantity=2
Authorization: Bearer <token>
```

Response `200` — updated cart:
```json
{
  "userId": 1,
  "items": {
    "1": 2
  }
}
```

---

## Step 7 — Review Cart

**GET** `/api/orders/cart`

```http
GET http://localhost:8080/api/orders/cart
Authorization: Bearer <token>
```

Shows all items (`medicineId -> quantity`) currently in the cart. Good place to let the user confirm before checkout.

---

## Step 8 — Checkout

**POST** `/api/orders/checkout`

```http
POST http://localhost:8080/api/orders/checkout
Authorization: Bearer <token>
Content-Type: application/json

{
  "shippingAddress": "123 Main St, City",
  "prescriptionId": 10,
  "idempotencyKey": "unique-key-abc123"
}
```

- `prescriptionId` — omit if no prescription medicines in cart
- `idempotencyKey` — optional but recommended to prevent duplicate orders on retry

Response `201` — the created order:
```json
{
  "id": 5,
  "userId": 1,
  "status": "PENDING",
  "totalAmount": 37.50,
  "shippingAddress": "123 Main St, City",
  "items": [...]
}
```

> Save the order `id` for payment.

---

## Step 9 — Pay

**POST** `/api/orders/{id}/pay`

```http
POST http://localhost:8080/api/orders/5/pay
Authorization: Bearer <token>
```

Response `200`:
```json
{
  "id": 1,
  "orderId": 5,
  "amount": 37.50,
  "status": "SUCCESS",
  "createdAt": "2026-03-26T10:05:00"
}
```

Payment done. Order is placed.

---

## Step 10 — Confirm Order & Payment (optional)

Check the order status and payment receipt.

**GET** `/api/orders/5`
```http
GET http://localhost:8080/api/orders/5
Authorization: Bearer <token>
```

**GET** `/api/orders/5/payment`
```http
GET http://localhost:8080/api/orders/5/payment
Authorization: Bearer <token>
```

---

## Full Flow Summary

```
POST   /api/auth/signup
  └─> save JWT token

GET    /api/catalog/categories          (browse)
GET    /api/catalog/medicines           (search)
GET    /api/catalog/medicines/{id}      (view item)

POST   /api/catalog/prescriptions/upload     (if prescription required)
GET    /api/catalog/prescriptions/{id}/status  (poll until APPROVED)

POST   /api/orders/cart/items           (add items, repeat per medicine)
GET    /api/orders/cart                 (review cart)

POST   /api/orders/checkout             (place order)
POST   /api/orders/{id}/pay             (pay)

GET    /api/orders/{id}                 (confirm order)
GET    /api/orders/{id}/payment         (confirm payment)
```
