# Admin Service — Deep Dive Documentation

## What is the Admin Service?

The Admin Service is the **admin control panel backend**. It doesn't have its own database. Instead, it acts as an **orchestration layer** — it receives requests from admin users, forwards them to the appropriate downstream services (auth-service, catalog-service, order-service) via Feign clients, and returns the results.

Port: `8084`

This design means:
- All business logic and data stays in the owning service
- The admin-service is a thin proxy with no state of its own
- If you need to add a new admin feature, you add an internal endpoint to the owning service and a Feign call here

---

## Application Entry Point

### `AdminServiceApplication.java`

```java
@SpringBootApplication
@EnableDiscoveryClient   // registers with Eureka
@EnableFeignClients      // enables Feign HTTP clients
```

No `@EnableAsync` — admin operations are synchronous. No `@EnableScheduling` — no background jobs.

---

## Config

### `config/OpenApiConfig.java`

Points Swagger at the gateway. Same pattern as all other services.

---

## Feign Clients

These are the most important files in the admin-service. They define how the admin-service talks to other services.

### `client/AuthClient.java`

Calls `auth-service` internal endpoints:

```java
@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/api/auth/internal/users")
    PagedResponse<UserProfileResponse> listUsers(...);

    @GetMapping("/api/auth/internal/users/{id}")
    UserProfileResponse getUserById(@PathVariable Long id);

    @PatchMapping("/api/auth/internal/users/{id}/status")
    UserProfileResponse updateUserStatus(...);

    @PostMapping("/api/auth/internal/users")
    UserProfileResponse createUser(...);
}
```

`@FeignClient(name = "auth-service")` — Feign resolves `auth-service` to an actual IP/port via Eureka. No hardcoded URLs.

---

### `client/CatalogClient.java`

The largest Feign client — calls both public and internal catalog endpoints:

**Public reads** (no auth needed):
- `getMedicineById(id)` — via public endpoint
- `getAllCategories()` — via public endpoint

**Internal medicine operations** (via `/api/catalog/internal/medicines`):
- Full list including inactive, create, update, deactivate
- Low-stock and expiring-soon lists

**Internal batch operations** (via `/api/catalog/internal/batches`):
- `adjustBatchStock()` — passes `X-User-Id` header so catalog-service knows which admin performed the adjustment

**Internal category operations** (via `/api/catalog/internal/categories`):
- Create, update, deactivate

**Internal prescription operations** (via `/api/catalog/internal/prescriptions`):
- Pending review queue, approve/reject, full list

---

### `client/OrderClient.java`

Calls order-service internal endpoints:

```java
@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping("/api/orders/internal/all")
    PagedResponse<OrderResponse> getAllOrders(...);

    @PatchMapping("/api/orders/internal/{id}/status/{status}")
    OrderResponse updateOrderStatus(..., @RequestHeader("X-User-Id") Long adminId);

    @GetMapping("/api/orders/internal/dashboard")
    DashboardDto getDashboard();

    @GetMapping("/api/orders/internal/reports/sales")
    SalesReportDto getSalesReport();
}
```

The `@RequestHeader("X-User-Id") Long adminId` on status update passes the admin's ID to order-service so it can log who made the change in `OrderStatusLog`.

---

## Controllers

### `controller/AdminUserController.java`

Path: `/api/admin/users`

| Endpoint | What it does | Calls |
|---|---|---|
| `GET /` | Paginated user list with filters | `AuthClient.listUsers()` |
| `GET /{id}` | Get user by ID | `AuthClient.getUserById()` |
| `PATCH /{id}/status` | Activate/suspend user | `AuthClient.updateUserStatus()` |
| `POST /` | Create user with any role | `AuthClient.createUser()` |

---

### `controller/AdminMedicineController.java`

Path: `/api/admin` (medicines, categories, prescriptions, batches)

This is the largest controller. Covers:

**Medicines:**
- List all (including inactive), get by ID, create, update, deactivate
- Low-stock alert list
- Expiring-soon list

**Batch stock:**
- `PATCH /batches/{batchId}/stock` — manual stock adjustment, passes `X-User-Id` to catalog-service for audit

**Categories:**
- List, get by ID, create, update, deactivate

**Prescriptions:**
- Pending review queue
- Approve/reject a prescription
- Full prescription list with filters

---

### `controller/AdminOrderController.java`

Path: `/api/admin/orders`

| Endpoint | What it does |
|---|---|
| `GET /` | All orders with status/userId filters |
| `GET /{id}` | Full order details |
| `PATCH /{id}/status/{status}` | Transition order to any valid status |
| `PATCH /{id}/cancel` | Admin cancel with note |

---

### `controller/AdminReportController.java`

Path: `/api/admin`

| Endpoint | What it does |
|---|---|
| `GET /dashboard` | Aggregated KPIs |
| `GET /reports/sales` | Sales report |

---

## Services

The services in admin-service are intentionally thin — they just delegate to Feign clients.

### `service/AdminUserService.java`

Delegates all calls to `AuthClient`. No business logic.

### `service/AdminMedicineService.java`

Delegates all calls to `CatalogClient`. One validation: `adjustStock()` checks that `batchId` is provided before calling the Feign client.

### `service/AdminOrderService.java`

Delegates all calls to `OrderClient`. No business logic.

### `service/AdminReportService.java`

The only service with actual logic — it **aggregates data from multiple services**:

```java
public DashboardDto getDashboard() {
    DashboardDto orderDashboard = orderClient.getDashboard();       // from order-service
    long lowStockCount = catalogClient.getLowStockMedicines(null).size();  // from catalog-service
    long expiringCount = catalogClient.getExpiringSoon(null, 90).size();   // from catalog-service
    long pendingRx = catalogClient.getPendingQueue(null, 0, 1).getTotalElements(); // from catalog-service

    return new DashboardDto(
        orderDashboard.getTotalOrders(),
        orderDashboard.getTodayRevenue(),
        pendingRx,
        lowStockCount,
        expiringCount,
        orderDashboard.getRecentOrders()
    );
}
```

This is the admin dashboard — it pulls from order-service for order stats and catalog-service for inventory alerts, then combines them into one response.

---

## DTOs

The admin-service has its own DTO classes that mirror the response shapes from downstream services. This is intentional — it decouples the admin-service from internal entity changes in other services. If catalog-service renames a field internally, only the Feign client mapping needs updating, not the admin API contract.

Key DTOs:
- `MedicineResponse`, `CategoryResponse`, `PrescriptionResponse` — mirror catalog-service responses
- `OrderResponse`, `OrderItemResponse` — mirror order-service responses
- `UserProfileResponse` — mirrors auth-service response
- `DashboardDto`, `SalesReportDto` — aggregated report shapes
- `PagedResponse<T>` — generic paginated wrapper

---

### `exception/GlobalExceptionHandler.java`

Admin-service has a special exception handler for **Feign errors**:

```java
@ExceptionHandler(FeignException.NotFound.class)
public ResponseEntity<...> handleFeignNotFound(FeignException.NotFound ex) {
    return buildError(HttpStatus.NOT_FOUND, "Resource not found", request);
}

@ExceptionHandler(FeignException.class)
public ResponseEntity<...> handleFeignException(FeignException ex) {
    return buildError(HttpStatus.BAD_GATEWAY, "Upstream service error: ...", request);
}
```

When a downstream service returns 404, the admin-service propagates it as 404. For other Feign errors (service down, timeout), it returns 502 Bad Gateway. This gives the admin frontend meaningful error messages.

---

### `resources/application.yaml`

```yaml
server:
  port: 8084

spring:
  application:
    name: admin-service
  # No datasource — admin-service has no database
```

Notable: no `spring.datasource` config. The admin-service has no database of its own.

---

### `pom.xml`

Key dependencies:

| Dependency | Purpose |
|---|---|
| `spring-cloud-starter-openfeign` | HTTP clients for all downstream services |
| `spring-boot-starter-web` | REST controllers |
| `spring-boot-starter-validation` | `@Valid` on request bodies |
| No JPA, no RabbitMQ | No database, no messaging |

---

## Architecture Summary

```
Admin User (ADMIN role JWT)
  │
  ▼
API Gateway (validates ADMIN role)
  │
  ▼
Admin Service (port 8084)
  │
  ├── AuthClient (Feign) ──────────────→ auth-service /api/auth/internal/**
  │
  ├── CatalogClient (Feign) ───────────→ catalog-service /api/catalog/internal/**
  │                                      catalog-service /api/catalog/** (public reads)
  │
  └── OrderClient (Feign) ─────────────→ order-service /api/orders/internal/**
```

The admin-service is the only service that calls internal endpoints of other services. Regular customers never reach those endpoints — the gateway blocks them.
