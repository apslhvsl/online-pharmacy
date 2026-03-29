package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.*;
import com.pharmacy.admin.service.AdminMedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMedicineController {

    private final AdminMedicineService adminMedicineService;

    // ── Medicine endpoints ───────────────────────────────────────────

    @Operation(summary = "List all medicines", description = "Returns a paginated list of all medicines including inactive ones, with optional filters for name, category, prescription requirement, and stock status")
    @GetMapping("/medicines")
    public ResponseEntity<PagedResponse<MedicineResponse>> getAllMedicines(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminMedicineService.getAllMedicines(q, categoryId, requiresPrescription, inStock, page, size));
    }

    @Operation(summary = "Get medicine by ID", description = "Returns the full details of a medicine by its ID, including inactive medicines")
    @GetMapping("/medicines/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.getMedicineById(id));
    }

    @Operation(summary = "Create a new medicine", description = "Creates a new medicine entry in the catalog and records the creating admin")
    @PostMapping("/medicines")
    public ResponseEntity<MedicineResponse> createMedicine(
            @RequestBody MedicineCreateRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMedicineService.createMedicine(request, adminId));
    }

    @Operation(summary = "Update a medicine", description = "Replaces all fields of an existing medicine with the provided data")
    @PutMapping("/medicines/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable Long id,
            @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.ok(adminMedicineService.updateMedicine(id, request));
    }

    @Operation(summary = "Deactivate a medicine", description = "Marks a medicine as inactive so it no longer appears in the public catalog")
    @PatchMapping("/medicines/{id}/deactivate")
    public ResponseEntity<MedicineResponse> deactivateMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.deactivateMedicine(id));
    }

    @Operation(summary = "Get low-stock medicines", description = "Returns medicines whose total available stock is below the specified threshold")
    @GetMapping("/medicines/low-stock")
    public ResponseEntity<List<MedicineResponse>> getLowStock(
            @RequestParam(required = false) Integer stockLessThan) {
        return ResponseEntity.ok(adminMedicineService.getLowStockMedicines(stockLessThan));
    }

    @Operation(summary = "Get medicines expiring soon", description = "Returns medicines with batches expiring before the given date or within the specified number of days")
    @GetMapping("/medicines/expiring")
    public ResponseEntity<List<MedicineResponse>> getExpiring(
            @RequestParam(required = false) String expiryBefore,
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(adminMedicineService.getExpiringSoon(expiryBefore, days));
    }

    // ── Batch stock adjustment ────────────────────────────────────────

    @Operation(summary = "Adjust batch stock", description = "Manually increases or decreases the stock quantity of a specific inventory batch and records the admin who performed the adjustment")
    @PatchMapping("/batches/{batchId}/stock")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Long batchId,
            @RequestBody StockAdjustRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        request.setBatchId(batchId);
        adminMedicineService.adjustStock(batchId, request, adminId);
        return ResponseEntity.noContent().build();
    }

    // ── Category endpoints ───────────────────────────────────────────

    @Operation(summary = "List all categories", description = "Returns all medicine categories including inactive ones")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(adminMedicineService.getAllCategories());
    }

    @Operation(summary = "Get category by ID", description = "Returns the details of a single category by its ID")
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.getCategoryById(id));
    }

    @Operation(summary = "Create a new category", description = "Creates a new medicine category in the catalog")
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMedicineService.createCategory(request));
    }

    @Operation(summary = "Update a category", description = "Replaces all fields of an existing category with the provided data")
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(adminMedicineService.updateCategory(id, request));
    }

    @Operation(summary = "Deactivate a category", description = "Marks a category as inactive so it no longer appears in the public catalog")
    @PatchMapping("/categories/{id}/deactivate")
    public ResponseEntity<CategoryResponse> deactivateCategory(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.deactivateCategory(id));
    }

    // ── Prescription endpoints ───────────────────────────────────────

    @Operation(summary = "Get pending prescription review queue", description = "Returns a paginated list of prescriptions awaiting admin review, optionally filtered by user")
    @GetMapping("/prescriptions/queue")
    public ResponseEntity<PagedResponse<PrescriptionResponse>> getPendingQueue(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminMedicineService.getPendingQueue(userId, page, size));
    }

    @Operation(summary = "Review a prescription", description = "Approves or rejects a customer prescription and records the reviewing admin")
    @PatchMapping("/prescriptions/{id}/review")
    public ResponseEntity<PrescriptionResponse> reviewPrescription(
            @PathVariable Long id,
            @RequestBody PrescriptionReviewRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(adminMedicineService.reviewPrescription(id, request, adminId));
    }

    @Operation(summary = "List all prescriptions", description = "Returns a paginated list of all prescriptions with optional filters for status and user")
    @GetMapping("/prescriptions")
    public ResponseEntity<PagedResponse<PrescriptionResponse>> getAllPrescriptions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminMedicineService.getAllPrescriptions(status, userId, page, size));
    }
}
