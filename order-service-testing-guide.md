# Order Service — User Endpoint Testing Guide

Base URL: `http://localhost:8080`  
All requests need a valid JWT: `-H "Authorization: Bearer <token>"`

Login first to get a token:
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Test@123"}'
```
Save the `accessToken` from the response. Replace `<TOKEN>` in all examples below.

---

## 1. Addresses

### List addresses
```bash
curl http://localhost:8080/api/orders/addresses \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: array of saved addresses. John has 2 from seed data.

### Add address
```bash
curl -X POST http://localhost:8080/api/orders/addresses \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home",
    "line1": "10 Park Street",
    "line2": "Floor 2",
    "city": "Delhi",
    "state": "Delhi",
    "pincode": "110001",
    "isDefault": false
  }'
```
Expected: 201 with the new address including its `id`.

### Update address
```bash
curl -X PUT http://localhost:8080/api/orders/addresses/<addressId> \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home Updated",
    "line1": "10 Park Street",
    "city": "Delhi",
    "state": "Delhi",
    "pincode": "110002",
    "isDefault": true
  }'
```

### Delete address
```bash
curl -X DELETE http://localhost:8080/api/orders/addresses/<addressId> \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: 204 No Content.

---

## 2. Cart

### View cart
```bash
curl http://localhost:8080/api/orders/cart \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: cart with items (John has Paracetamol x2 and Vitamin C x1 from seed).

### Add item to cart
Get a batchId first from catalog:
```bash
curl "http://localhost:8080/api/catalog/medicines?page=0&size=5" \
  -H "Authorization: Bearer <TOKEN>"
```
Then add a batch:
```bash
curl -X POST http://localhost:8080/api/orders/cart/items \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "batchId": <batchId>,
    "quantity": 2
  }'
```
Expected: updated cart with the new item.

### Update item quantity
```bash
curl -X PUT http://localhost:8080/api/orders/cart/items/<batchId> \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'
```
Tip: send `"quantity": 0` to remove the item.

### Remove specific item
```bash
curl -X DELETE http://localhost:8080/api/orders/cart/items/<batchId> \
  -H "Authorization: Bearer <TOKEN>"
```

### Clear entire cart
```bash
curl -X DELETE http://localhost:8080/api/orders/cart \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: 204 No Content.

---

## 3. Checkout (4-step flow)

Make sure the cart has items before starting.

### Step 1 — Start checkout
```bash
curl -X POST http://localhost:8080/api/orders/checkout/start \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: 201 with `orderId` and `status: PENDING`. Save the `orderId`.

### Step 2 — Set delivery address

Option A — use a saved address:
```bash
curl -X POST http://localhost:8080/api/orders/checkout/<orderId>/address \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"addressId": <addressId>}'
```

Option B — inline address (no saved address needed):
```bash
curl -X POST http://localhost:8080/api/orders/checkout/<orderId>/address \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "inlineAddress": {
      "label": "Home",
      "line1": "10 Park Street",
      "city": "Delhi",
      "state": "Delhi",
      "pincode": "110001"
    }
  }'
```

### Step 3 — Link prescription (only if cart has prescription medicines)
```bash
curl -X POST "http://localhost:8080/api/orders/checkout/<orderId>/prescription-link?prescriptionId=<prescriptionId>" \
  -H "Authorization: Bearer <TOKEN>"
```
Skip this step if no prescription medicines are in the cart.

### Step 4 — Confirm order
```bash
curl -X POST http://localhost:8080/api/orders/checkout/<orderId>/confirm \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: order with `status: PAID` or `PAYMENT_PENDING` depending on payment method. Stock is deducted at this point.

---

## 4. Orders

### List my orders
```bash
curl "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### Filter by status
Valid statuses: `DRAFT`, `CHECKOUT_STARTED`, `PRESCRIPTION_PENDING`, `PRESCRIPTION_APPROVED`, `PAYMENT_PENDING`, `PAID`, `PACKED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CUSTOMER_CANCELLED`, `ADMIN_CANCELLED`, `RETURN_REQUESTED`, `REFUND_INITIATED`, `REFUND_COMPLETED`
```bash
curl "http://localhost:8080/api/orders?status=DELIVERED&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### Get order by ID
```bash
curl http://localhost:8080/api/orders/<orderId> \
  -H "Authorization: Bearer <TOKEN>"
```

### Cancel an order
Only works on orders in `PAID` or `PROCESSING` status:
```bash
curl -X PATCH http://localhost:8080/api/orders/<orderId>/cancel \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Changed my mind"}'
```

### Request a return
Only works on `DELIVERED` orders:
```bash
curl -X POST http://localhost:8080/api/orders/<orderId>/return \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Wrong item received"}'
```

### Reorder
Adds all items from a previous order back into the cart:
```bash
curl -X POST http://localhost:8080/api/orders/<orderId>/reorder \
  -H "Authorization: Bearer <TOKEN>"
```
Expected: updated cart. Out-of-stock items are silently skipped.

---

## 5. Payments

### Initiate payment
Call after confirming an order:
```bash
curl -X POST http://localhost:8080/api/orders/payments/initiate \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": <orderId>,
    "paymentMethod": "COD"
  }'
```
Valid values for `paymentMethod`: `COD`, `PREPAID`

### Get payment for an order
```bash
curl http://localhost:8080/api/orders/payments/<orderId> \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Quick test scenarios from seed data

| Scenario | What to test |
|---|---|
| John (`john@example.com`) | Has cart with items, 2 addresses, orders in DELIVERED + PAYMENT_PENDING |
| Jane (`jane@example.com`) | Has a PAID order with prescription medicine |
| Bob (`bob@example.com`) | SUSPENDED — login should be rejected |
| ORD-2026-0001 | DELIVERED — test return request |
| ORD-2026-0002 | PAYMENT_PENDING — test payment initiate |
| Paracetamol batches | 2 batches (PCM-B001, PCM-B002) — add both to cart to test FEFO deduction on confirm |
| Pantoprazole | Low stock (qty=5) — add 6 to cart, confirm should fail |
| Amoxicillin | Requires prescription — checkout step 3 is mandatory |
