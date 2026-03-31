# Catalog Service — Deep Dive Documentation

## What is the Catalog Service?

The Catalog Service owns the **medicine catalog, categories, inventory batches, and prescriptions**. It is the source of truth for what medicines exist, how much stock is available, and whether a customer's prescription has been approved.

Port: `8082`

It has two layers of controllers:
- **Public controllers** — accessible through the gateway by anyone (GET) or authenticated users (prescriptions)
- **Internal controllers** — at `/api/catalog/internal/**`, blocked by the gateway, only callable by admin-service and order-service via Feign

---

## File-by-File Breakdown

---

### `CatalogServiceApplication.java`

```java
@SpringBootApplication
@EnableDiscoveryClient
```

`@EnableDiscoveryClient` registers this service with Eureka so other services can find it by name (`lb://catalog-service`).

---

### `config/OpenApiConfig.java`

Same pattern as other services — points Swagger server URL at the gateway (`http://localhost:8080`) so test requests go through the full auth flow.

---

## Controllers

### `controller/MedicineController.java` — Public

Path: `/api/catalog/medicines`

| Endpoint | Method | Auth | What it does |
|---|---|---|---|
| `/api/catalog/medicines` | GET | None | Paginated search with filters: name, category, prescription flag, price range |
| `/api/catalog/medicines/{id}` | GET | None | Single medicine details |
| `/api/catalog/medicines/{id}/stock-check` | GET | None | Returns availability + best batch ID for a quantity |

These are read-only and public — anyone can browse the catalog without logging in.

---

### `controller/CategoryController.java` — Public

Path: `/api/catalog/categories`

| Endpoint | Method | Auth | What it does |
|---|---|---|---|
| `/api/catalog/categories` | GET | None | All active categories |
| `/api/catalog/categories/{id}` | GET | None | Single category |

---

### `controller/PrescriptionController.java` — Authenticated Customers

Path: `/api/catalog/prescriptions`

| Endpoint | Method | Auth | What it does |
|---|---|---|---|
| `/upload` | POST | Customer | Upload a prescription file (PDF/JPG/PNG, max 5MB) |
| `/` | GET | Customer | List own prescriptions |
| `/{id}` | GET | Customer | Get own prescription by ID |
| `/{id}/file` | GET | Customer | Download the prescription file |

The `X-User-Id` header (injected by gateway) is used to enforce ownership — customers can only see their own prescriptions.

---

### `controller/InternalMedicineController.java` — Internal (Admin Service only)

Path: `/api/catalog/internal/medicines`

Blocked from external access by the gateway. Provides admin-level medicine operations:
- Full list including inactive medicines
- Create, update, deactivate medicines
- Low-stock alert list
- Expiring-soon list

---

### `controller/InternalCategoryController.java` — Internal (Admin Service only)

Path: `/api/catalog/internal/categories`

Create, update, deactivate categories.

---

### `controller/InternalPrescriptionController.java` — Internal (Admin + Order Service)

Path: `/api/catalog/internal/prescriptions`

- Pending prescription review queue (for admin)
- Approve/reject a prescription (for admin)
- Full prescription list with filters (for admin)
- `GET /{id}` — used by order-service to validate prescription status at checkout (no ownership check)

---

### `controller/InternalBatchController.java` — Internal (Admin + Order Service)

Path: `/api/catalog/internal/batches`

This is the most critical internal controller — order-service calls it during checkout:

| Endpoint | Called by | What it does |
|---|---|---|
| `GET /medicine/{medicineId}` | Admin | List all batches for a medicine |
| `POST /` | Admin | Create a new inventory batch |
| `PUT /{batchId}` | Admin | Update batch details |
| `PATCH /{batchId}/stock` | Admin | Manual stock adjustment with audit trail |
| `POST /{batchId}/deduct` | Order Service | Deduct stock when order is confirmed |
| `GET /{batchId}/stock-check` | Order Service | Check if a specific batch has enough stock |

---

## Services

### `service/MedicineService.java`

Core medicine business logic.

**`getMedicines()`** — public search. Only returns `active = true` medicines. Enriches each result with current stock via `enrichWithStock()`.

**`checkStock()`** — implements **FEFO (First Expired, First Out)**. Queries batches ordered by expiry date ascending, picks the earliest-expiring batch as the "best batch". This ensures older stock is sold first, reducing waste.

```java
Long bestBatchId = batches.isEmpty() ? null : batches.get(0).getId(); // FEFO
```

**`getMedicinesAdmin()`** — includes inactive medicines, used by admin-service.

**`enrichWithStock()`** — calls `batchRepository.sumAvailableStock()` to add the total available quantity to each medicine DTO. Only counts non-expired batches.

---

### `service/CategoryService.java`

Straightforward CRUD for categories. `deleteCategory()` prevents deletion if the category has medicines assigned to it (throws `IllegalStateException`). Soft-delete via `active = false`.

---

### `service/PrescriptionService.java`

Handles file upload and prescription lifecycle.

**`uploadPrescription()`**:
1. Validates file type (PDF, JPG, PNG only) and size (max 5MB)
2. Creates a user-specific directory: `uploads/prescriptions/{userId}/`
3. Saves file with a UUID prefix to prevent filename collisions
4. Creates a `Prescription` entity with `PENDING` status

**`reviewPrescription()`** — admin sets status to `APPROVED` or `REJECTED`, records `reviewedBy` (admin ID) and `reviewedAt` timestamp.

**`getPrescriptionById()`** — the `isAdmin` flag controls ownership enforcement. When called by order-service (via internal endpoint), `isAdmin = true` skips the ownership check.

---

### `service/InventoryBatchService.java`

Manages inventory batches with full audit trail.

**`adjustBatchStock()`** — manual stock adjustment. Records a full audit entry in `inventory_audit` table: before quantity, after quantity, reason, and who performed it.

**`deductBatchStock()`** — called by order-service during checkout confirmation. Directly deducts from a specific batch.

**`deductStock()`** — deducts from a medicine across multiple batches using FEFO order. Used when you don't care which specific batch, just need to reduce total stock.

**`checkStock()`** — validates a specific batch has enough quantity AND hasn't expired.

---

## Entities

### `Medicine.java`

Maps to `medicines` table. Key fields:
- `active` — soft delete flag. Inactive medicines don't appear in public search.
- `requiresPrescription` — if true, customers must upload and get an approved prescription before checkout.
- `reorderLevel` — threshold for low-stock alerts (default 10).
- `@ManyToOne` to `Category` — lazy loaded.

### `Category.java`

Maps to `categories` table. Has a `slug` field (URL-friendly name, unique). `@OneToMany` to medicines (lazy).

### `Prescription.java`

Maps to `prescriptions` table. Key fields:
- `userId` — owner (no FK to users table — cross-service, stored as plain Long)
- `filePath` — absolute path on disk where the file is stored
- `status` — `PENDING`, `APPROVED`, `REJECTED`
- `validTill` — set by admin when approving; order-service checks this at checkout
- `reviewedBy` / `reviewedAt` — audit trail for who approved/rejected

### `InventoryBatch.java`

Maps to `inventory_batches`. Each batch represents a physical shipment of a medicine:
- `batchNumber` — manufacturer's batch identifier
- `expiryDate` — used for FEFO ordering and expiry checks
- `price` — batch-level price (can differ from medicine's base price)
- `quantity` — current stock count

### `InventoryAudit.java`

Maps to `inventory_audit`. Immutable audit log of every manual stock adjustment. Records `stockBefore`, `stockAfter`, `adjustment`, `reason`, and `performedBy` (admin ID).

---

### `mapper/MedicineMapper.java` and `mapper/PrescriptionMapper.java`

MapStruct mappers — compile-time code generation for entity-to-DTO conversion. Faster than reflection-based mappers like ModelMapper. The `@Mapper(componentModel = "spring")` annotation makes them Spring beans.

---

### `exception/GlobalExceptionHandler.java`

| Exception | HTTP Status |
|---|---|
| `EntityNotFoundException` | 404 |
| `InvalidFileTypeException` | 415 Unsupported Media Type |
| `IllegalStateException` | 409 Conflict |
| `IllegalArgumentException` | 400 Bad Request |
| `SecurityException` | 403 Forbidden |
| `Exception` (catch-all) | 500 |

---

### `resources/application.yml`

Key settings:

```yaml
server:
  port: 8082

spring:
  servlet:
    multipart:
      max-file-size: 10MB      # gateway also allows 10MB
      max-request-size: 10MB

prescription:
  upload-dir: uploads/prescriptions   # injected into PrescriptionService
```

The `upload-dir` is a relative path. In Docker, the `uploads/` directory is mounted as a named volume (`uploads-data`) so files persist across container restarts.

---

### `pom.xml`

Key dependencies:

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-data-jpa` | JPA/Hibernate for PostgreSQL |
| `mapstruct` + `mapstruct-processor` | Compile-time entity-to-DTO mapping |
| `postgresql` | JDBC driver |
| No RabbitMQ | Catalog service doesn't publish events |

---

## Inventory Flow

```
Admin creates medicine → Medicine entity (active=true, requiresPrescription=?)
Admin creates batch   → InventoryBatch (quantity=N, expiryDate=X)

Customer browses      → MedicineController.getMedicines()
                        → enrichWithStock() sums all non-expired batches

Customer adds to cart → OrderService calls CatalogClient.checkStock(medicineId, qty)
                        → FEFO: picks earliest-expiring batch with enough stock
                        → Returns batchId to cart

Checkout confirm      → OrderService calls CatalogClient.checkBatchStock(batchId, qty)
                        → Validates specific batch still has stock
                        → OrderService calls CatalogClient.deductBatchStock(batchId, qty)
                        → Stock is permanently deducted
```
