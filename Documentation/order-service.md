# Order Service — Deep Dive Documentation

## What is the Order Service?

The Order Service manages the full lifecycle of a customer's purchase: shopping cart, checkout flow, order management, payments, addresses, and returns. It is the most complex service in the system.

Port: `8083`

It communicates with:
- **catalog-service** (via Feign) — stock checks, batch deductions, prescription validation
- **auth-service** (via Feign) — fetching user email/name for notifications
- **RabbitMQ** — publishing order status change events to notification-service

---

## Application Entry Point

### `OrderServiceApplication.java`

```java
@SpringBootApplication
@EnableDiscoveryClient   // registers with Eureka
@EnableFeignClients      // enables Feign HTTP clients
@EnableAsync             // enables @Async for non-blocking event publishing
```

`@EnableAsync` is important — it allows `OrderEventPublisher.publishOrderUpdate()` to run in a background thread so a notification failure never blocks or fails an order operation.

---

## Config

### `config/RabbitMQConfig.java`

Publisher-only config. Defines the exchange and routing key for order update events:

```java
public static final String EXCHANGE          = "pharmacy.notifications";
public static final String ORDER_ROUTING_KEY = "order.update";
```

Uses `Jackson2JsonMessageConverter` so messages are JSON, not Java binary.

### `config/OpenApiConfig.java`

Points Swagger at the gateway URL. Same pattern as other services.

---

## Controllers

### `controller/CartController.java`

Path: `/api/orders/cart`

| Endpoint | Method | What it does |
|---|---|---|
| `GET /` | GET | Get current cart with totals |
| `POST /items` | POST | Add medicine to cart (checks stock via Feign) |
| `PUT /items/{batchId}` | PUT | Update item quantity (set to 0 to remove) |
| `DELETE /items/{batchId}` | DELETE | Remove specific item |
| `DELETE /` | DELETE | Clear entire cart |

---

### `controller/CheckoutController.java`

Path: `/api/orders/checkout`

The checkout is a **4-step flow**:

| Step | Endpoint | What it does |
|---|---|---|
| 1 | `POST /start` | Creates a CHECKOUT_STARTED order from the cart |
| 2 | `POST /{orderId}/address` | Attaches a delivery address |
| 3 | `POST /{orderId}/prescription-link` | Links an approved prescription (if required) |
| 4 | `POST /{orderId}/confirm` | Validates stock, deducts inventory, creates order items, transitions to PAYMENT_PENDING |

---

### `controller/OrderController.java`

Path: `/api/orders`

Customer order management:

| Endpoint | What it does |
|---|---|
| `GET /` | Paginated list of own orders, optional status filter |
| `GET /{id}` | Single order with items and payment |
| `PATCH /{id}/cancel` | Cancel an order (with reason) |
| `POST /{id}/reorder` | Re-add all items from a past order to cart |
| `POST /{id}/return` | Request a return (only for DELIVERED orders within 7 days) |
| `POST /payments/initiate/{orderId}` | Initiate payment for a PAYMENT_PENDING order |
| `GET /payments/{orderId}` | Get payment details for an order |

---

### `controller/AddressController.java`

Path: `/api/orders/addresses`

Full CRUD for saved delivery addresses. All operations are scoped to the authenticated user via `X-User-Id`.

---

### `controller/InternalOrderController.java`

Path: `/api/orders/internal`

Blocked from external access by the gateway. Called by admin-service only:

| Endpoint | What it does |
|---|---|
| `GET /all` | All orders with filters (status, userId, date range) |
| `GET /{id}` | Any order by ID (no ownership check) |
| `PATCH /{id}/status/{status}` | Transition order to any status |
| `PATCH /{id}/cancel` | Admin cancel (sets ADMIN_CANCELLED) |
| `GET /dashboard` | Aggregated stats for admin dashboard |
| `GET /reports/sales` | Sales report with top medicines |

---

## Services

### `service/CartService.java`

**`addItem()`** flow:
1. Calls `catalogClient.checkStock(medicineId, quantity)` — gets availability and the best batch ID (FEFO)
2. Calls `catalogClient.getMedicineById(medicineId)` — gets name and price for snapshot
3. Creates or updates a `CartItem` linked to the specific `batchId`

The cart stores a **price snapshot** at the time of adding. If the medicine price changes later, the cart still shows the price when the item was added.

**Tax calculation** — 5% GST applied in `toDto()`:
```java
BigDecimal taxAmount = subTotal.multiply(new BigDecimal("0.05"));
```

---

### `service/CheckoutService.java`

The most important service. Implements the 4-step checkout.

**Step 1 — `startCheckout()`**:
- Validates cart is not empty
- Creates an `Order` with `CHECKOUT_STARTED` status
- Generates order number: `RX-{year}-{id:05d}` (e.g., `RX-2026-00042`)
- Returns whether the cart requires a prescription

**Step 2 — `setAddress()`**:
- Accepts either an existing `addressId` or an inline address object
- If inline, saves the address first and uses the new ID

**Step 3 — `linkPrescription()`**:
- Attaches a `prescriptionId` to the order
- Sets status to `PRESCRIPTION_APPROVED`

**Step 4 — `confirmOrder()`** — the critical step:
1. **Prescription enforcement** — if any cart item requires a prescription:
   - Checks `prescriptionId` is set
   - Calls `catalogClient.getPrescriptionById()` to verify status is `APPROVED`
   - Checks prescription hasn't expired (`validTill`)
   - Checks prescription belongs to this user
2. **Stock validation** — for each cart item, calls `catalogClient.checkBatchStock(batchId, qty)` to verify the specific batch still has stock
3. **Price snapshot** — creates `OrderItem` records with the current price (locked in at checkout)
4. **Pricing calculation**:
   - Subtotal = sum of line totals
   - Tax = 5% of subtotal
   - Delivery = free if subtotal ≥ 500, otherwise 50
   - Total = subtotal + tax + delivery
5. **Stock deduction** — calls `catalogClient.deductBatchStock(batchId, qty)` for each item
6. **Cart cleared** — cart is emptied after successful confirmation

---

### `service/OrderService.java`

Handles order retrieval, status updates, cancellations, returns, and reporting.

**`cancelOrder()`** — validates the transition via `OrderStateMachine`, logs it, publishes an event.

**`requestReturn()`** — only allowed for `DELIVERED` orders within 7 days of delivery.

**`getDashboard()`** — aggregates total orders, today's revenue (from PAID orders), and 5 most recent orders.

**`getSalesReport()`** — iterates all DELIVERED orders, aggregates revenue and quantity sold per medicine, sorts by quantity descending.

**`toDto()`** — the shared order-to-DTO converter. Also fetches the associated payment record and embeds it in the response.

---

### `service/PaymentService.java`

**`initiatePayment()`**:
- Validates order is in `PAYMENT_PENDING` state
- Creates a `Payment` record with a UUID gateway transaction reference
- For **COD (Cash on Delivery)**: immediately marks payment as `PAID`, transitions order to `PAID`, publishes notification event
- For other methods (card, etc.): payment stays `PENDING` until a callback arrives

The payment callback endpoint (`/api/orders/payments/callback`) is in the public path list in the gateway — it's called by the payment provider, not by a logged-in user.

---

### `service/OrderStateMachine.java`

Enforces valid order status transitions. Every status change goes through `validate()` first.

```
DRAFT → CHECKOUT_STARTED → PAYMENT_PENDING → PAID → PACKED → OUT_FOR_DELIVERY → DELIVERED
                                                                                      ↓
                                                                              RETURN_REQUESTED
                                                                                      ↓
                                                                              REFUND_INITIATED
                                                                                      ↓
                                                                              REFUND_COMPLETED
```

Invalid transitions throw `InvalidStateTransitionException` (400 Bad Request). This prevents things like jumping from `DRAFT` directly to `DELIVERED`.

Full transition map:

| From | Allowed To |
|---|---|
| DRAFT | CHECKOUT_STARTED, CUSTOMER_CANCELLED |
| CHECKOUT_STARTED | PRESCRIPTION_PENDING, PAYMENT_PENDING, CUSTOMER_CANCELLED |
| PRESCRIPTION_PENDING | PRESCRIPTION_APPROVED, PRESCRIPTION_REJECTED |
| PRESCRIPTION_APPROVED | PAYMENT_PENDING, CUSTOMER_CANCELLED |
| PAYMENT_PENDING | PAID, PAYMENT_FAILED, CUSTOMER_CANCELLED |
| PAID | PACKED, ADMIN_CANCELLED, CUSTOMER_CANCELLED |
| PACKED | OUT_FOR_DELIVERY, ADMIN_CANCELLED, CUSTOMER_CANCELLED |
| OUT_FOR_DELIVERY | DELIVERED |
| DELIVERED | RETURN_REQUESTED |
| ADMIN_CANCELLED | REFUND_INITIATED |
| RETURN_REQUESTED | REFUND_INITIATED |
| REFUND_INITIATED | REFUND_COMPLETED |

---

### `service/OrderEventPublisher.java`

```java
@Async
public void publishOrderUpdate(Order order) {
    UserInfo user = authClient.getUserById(order.getUserId());
    // build event, publish to RabbitMQ
}
```

`@Async` means this runs in a Spring-managed thread pool, not the HTTP request thread. If the notification fails (RabbitMQ down, auth-service down), it logs a warning but **does not fail the order operation**. Notifications are non-critical.

---

## Entities

### `Order.java`

Maps to `orders` table. Key fields:
- `orderNumber` — human-readable ID like `RX-2026-00042`
- `userId` — owner (no FK to users table — cross-service)
- `addressId` — FK to addresses table within this service
- `prescriptionId` — FK to catalog-service's prescriptions (cross-service, stored as Long)
- `subtotal`, `taxAmount`, `deliveryCharge`, `totalAmount` — price breakdown
- `@OneToMany` to `OrderItem` — lazy loaded, cascade all

### `Cart.java`

Maps to `carts`. One cart per user (`userId` is unique). `@OneToMany` to `CartItem` with `orphanRemoval = true` — removing an item from the list deletes it from the database.

### `Payment.java`

Maps to `payments`. One-to-one with Order (unique FK). Stores `gatewayResponse` as JSONB for raw payment provider responses. Has `paidAt` and `refundedAt` timestamps.

### `OrderStatus.java`

Enum with 16 statuses covering the full lifecycle. Each status has a comment explaining when it's used.

### `IdempotencyKey.java`

Maps to `idempotency_keys`. Stores request keys with their responses to prevent duplicate operations (e.g., double-clicking checkout). Expires after 24 hours.

---

## Feign Clients

### `client/CatalogClient.java`

Calls catalog-service internal endpoints:
- `checkStock(medicineId, qty)` — medicine-level stock check
- `getMedicineById(medicineId)` — medicine info for cart snapshot
- `checkBatchStock(batchId, qty)` — batch-level stock check at checkout
- `deductBatchStock(batchId, qty)` — stock deduction at checkout
- `getPrescriptionById(prescriptionId)` — prescription validation at checkout

### `client/AuthClient.java`

Calls auth-service internal endpoint:
- `getUserById(userId)` — gets user email and name for notification events

---

### `exception/GlobalExceptionHandler.java`

| Exception | HTTP Status |
|---|---|
| `EntityNotFoundException` | 404 |
| `InsufficientStockException` | 409 Conflict |
| `InvalidStateTransitionException` | 400 Bad Request |
| `IllegalStateException` | 409 Conflict |
| `IllegalArgumentException` | 400 Bad Request |
| `SecurityException` | 403 Forbidden |

---

### `resources/application.yaml`

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
  jpa:
    hibernate:
      ddl-auto: update
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
```

---

### `pom.xml`

Key dependencies:

| Dependency | Purpose |
|---|---|
| `spring-cloud-starter-openfeign` | HTTP clients for catalog-service and auth-service |
| `spring-boot-starter-amqp` | RabbitMQ for publishing order events |
| `spring-boot-starter-data-jpa` | JPA/Hibernate for PostgreSQL |
| `postgresql` | JDBC driver |

---

## Full Checkout Flow

```
1. POST /api/orders/checkout/start
   → Cart validated, Order created (CHECKOUT_STARTED)
   ← { orderId, requiresPrescription }

2. POST /api/orders/checkout/{orderId}/address
   → Address attached to order

3. (If requiresPrescription)
   POST /api/orders/checkout/{orderId}/prescription-link?prescriptionId=X
   → Prescription validated, order status → PRESCRIPTION_APPROVED

4. POST /api/orders/checkout/{orderId}/confirm
   → Prescription re-validated (status, expiry, ownership)
   → Each batch stock re-checked
   → OrderItems created with price snapshot
   → Tax + delivery calculated
   → Stock deducted from catalog-service
   → Cart cleared
   → Order status → PAYMENT_PENDING

5. POST /api/orders/payments/initiate/{orderId}?paymentMethod=COD
   → Payment record created
   → COD: immediately PAID, notification published
   → Card: stays PENDING until callback
```
