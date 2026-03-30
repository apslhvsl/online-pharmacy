# User Flow Testing Guide — Signup to Payment

This guide walks through the complete customer journey end-to-end using `curl`.  
All requests go through the API Gateway at `http://localhost:8080`.

---

## Prerequisites

- Docker stack is running: `docker-compose up -d`
- `jq` is installed for JSON parsing
- Set the base URL: `export BASE="http://localhost:8080"`

---

## Flow A — OTC Medicine (No Prescription)

### Step 1: Sign Up

```bash
SIGNUP=$(curl -s -X POST $BASE/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "mobile": "9123456789",
    "password": "Pass@1234"
  }')

TOKEN=$(echo $SIGNUP | jq -r '.accessToken')
echo "Token: $TOKEN"
```

Password rules: min 8 chars, 1 uppercase, 1 digit, 1 special char (`@$!%*?&`).

---

### Step 2: Browse the Catalog

```bash
# List all medicines
curl -s "$BASE/api/catalog/medicines" | jq '.content[]'

# Search by name
curl -s "$BASE/api/catalog/medicines?q=paracetamol" | jq '.content[0]'

# Browse categories
curl -s "$BASE/api/catalog/categories" | jq '.'
```

Note the `id` (medicineId) and `batchId` from the medicine response — you need `batchId` to add to cart.

> If the catalog is empty, an admin needs to create medicines and inventory batches first. See the Admin Setup section at the bottom.

---

### Step 3: Add Items to Cart

```bash
# Add an item (replace batchId with actual value)
curl -s -X POST $BASE/api/orders/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"batchId": 5, "quantity": 2}'

# View cart
curl -s $BASE/api/orders/cart \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

The cart response shows `subtotal`, `taxAmount` (5% GST), `total`, and `requiresPrescription`.

---

### Step 4: Save a Delivery Address

```bash
ADDRESS=$(curl -s -X POST $BASE/api/orders/addresses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Home",
    "line1": "123 MG Road",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "isDefault": true
  }')

ADDRESS_ID=$(echo $ADDRESS | jq -r '.id')
echo "Address ID: $ADDRESS_ID"
```

---

### Step 5: Start Checkout

```bash
SESSION=$(curl -s -X POST $BASE/api/orders/checkout/start \
  -H "Authorization: Bearer $TOKEN")

ORDER_ID=$(echo $SESSION | jq -r '.orderId')
echo "Order ID: $ORDER_ID"
echo "Requires Prescription: $(echo $SESSION | jq -r '.requiresPrescription')"
```

If `requiresPrescription` is `true`, follow Flow B instead.

---

### Step 6: Attach Address to Order

```bash
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"addressId\": $ADDRESS_ID}"
```

Alternatively, provide an inline address without saving it:

```bash
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inlineAddress": {
      "label": "Home",
      "line1": "123 MG Road",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560001",
      "isDefault": false
    }
  }'
```

---

### Step 7: Confirm Order

This validates stock, snapshots prices, deducts inventory, and moves the order to `PAYMENT_PENDING`.

```bash
curl -s -X POST $BASE/api/orders/checkout/$ORDER_ID/confirm \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

Expected `status`: `PAYMENT_PENDING`  
Delivery is free for orders above ₹500, otherwise a delivery charge applies.

---

### Step 8: Pay

```bash
# Cash on Delivery
curl -s -X POST $BASE/api/orders/payments/initiate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"orderId\": $ORDER_ID, \"paymentMethod\": \"COD\"}" | jq '.'
```

Payment methods: `COD`, `PREPAID`, `WALLET`

Expected payment `status`: `PAID`

---

### Step 9: Verify Order Status

```bash
curl -s $BASE/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.status'
# → "PAID"
```

---

## Flow B — Prescription-Required Medicine

Follow Steps 1–2 from Flow A, then continue here.

### Step 3: Upload Prescription

```bash
RX=$(curl -s -X POST $BASE/api/catalog/prescriptions/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/prescription.pdf")

RX_ID=$(echo $RX | jq -r '.id')
echo "Prescription ID: $RX_ID, Status: $(echo $RX | jq -r '.status')"
# → Status: PENDING
```

Accepted formats: PDF, JPG, PNG. Max size: 5MB.

---

### Step 4: Admin Approves the Prescription

This requires an admin token. Get one:

```bash
ADMIN_TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pharmacy.com","password":"Admin@1234"}' \
  | jq -r '.accessToken')
```

Then approve:

```bash
curl -s -X PATCH $BASE/api/admin/prescriptions/$RX_ID/review \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED", "remarks": "Valid prescription from licensed doctor"}'
```

---

### Step 5: Add Rx Medicine to Cart

```bash
curl -s -X POST $BASE/api/orders/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"batchId": 12, "quantity": 1}'
```

The cart will show `requiresPrescription: true`.

---

### Steps 6–7: Save Address and Start Checkout

Same as Flow A Steps 4–5. After starting checkout, `requiresPrescription` will be `true` in the response.

---

### Step 8: Link Prescription to Order

```bash
curl -s -X POST "$BASE/api/orders/checkout/$ORDER_ID/prescription-link?prescriptionId=$RX_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Expected `status`: `PRESCRIPTION_APPROVED`

---

### Steps 9–11: Confirm, Pay, Verify

Same as Flow A Steps 7–9.

---

## Cart Management (Optional)

```bash
# Update item quantity
curl -s -X PUT $BASE/api/orders/cart/items/5 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'

# Remove a single item (set quantity to 0)
curl -s -X PUT $BASE/api/orders/cart/items/5 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 0}'

# Or delete explicitly
curl -s -X DELETE $BASE/api/orders/cart/items/5 \
  -H "Authorization: Bearer $TOKEN"

# Clear entire cart
curl -s -X DELETE $BASE/api/orders/cart \
  -H "Authorization: Bearer $TOKEN"
```

---

## Cancel an Order

Only allowed before payment (`DRAFT`, `CHECKOUT_STARTED`, `PRESCRIPTION_APPROVED`, `PAYMENT_PENDING`).

```bash
curl -s -X PATCH $BASE/api/orders/$ORDER_ID/cancel \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Changed my mind"}'
```

---

## Order Status Reference

```
DRAFT → CHECKOUT_STARTED → PAYMENT_PENDING → PAID → PACKED → OUT_FOR_DELIVERY → DELIVERED
                         ↘ PRESCRIPTION_PENDING → PRESCRIPTION_APPROVED → PAYMENT_PENDING
```

Terminal states: `CUSTOMER_CANCELLED`, `PAYMENT_FAILED`, `REFUND_COMPLETED`, `PRESCRIPTION_REJECTED`

---

## Admin Setup (if catalog is empty)

Before testing, an admin must seed at least one medicine with stock.

```bash
# 1. Create a category
curl -s -X POST $BASE/api/admin/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Pain Relief", "slug": "pain-relief"}'

# 2. Create a medicine (use the categoryId from above)
curl -s -X POST $BASE/api/admin/medicines \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paracetamol 500mg",
    "categoryId": 1,
    "price": 25.00,
    "requiresPrescription": false,
    "manufacturer": "Sun Pharma",
    "strength": "500mg",
    "packSize": "10 tablets",
    "reorderLevel": 10
  }'

# 3. Add stock to a batch (use the batchId from the medicine response)
curl -s -X PATCH $BASE/api/admin/batches/1/stock \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"adjustment": 200, "reason": "Initial stock"}'
```

---

## Error Reference

| Status | Meaning |
|--------|---------|
| `400`  | Validation error or invalid state transition |
| `401`  | Missing or expired token |
| `403`  | Insufficient role (e.g., customer hitting admin route) |
| `404`  | Resource not found |
| `409`  | Business conflict — insufficient stock, wrong state, prescription not approved |
| `415`  | Unsupported file type for prescription upload |
