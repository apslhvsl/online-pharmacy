package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.*;
import com.pharmacy.admin.service.AdminMedicineService;
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

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(adminMedicineService.getAllMedicines());
    }

    @GetMapping("/medicines/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.getMedicineById(id));
    }

    @PostMapping("/medicines")
    public ResponseEntity<MedicineResponse> createMedicine(
            @RequestBody MedicineCreateRequest request,
            @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMedicineService.createMedicine(request, adminId));
    }

    @PutMapping("/medicines/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable Long id,
            @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.ok(adminMedicineService.updateMedicine(id, request));
    }

    @PatchMapping("/medicines/{id}/stock")
    public ResponseEntity<MedicineResponse> adjustStock(
            @PathVariable Long id,
            @RequestBody StockAdjustRequest request,
            @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(adminMedicineService.adjustStock(id, request, adminId));
    }

    @PatchMapping("/medicines/{id}/deactivate")
    public ResponseEntity<MedicineResponse> deactivateMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.deactivateMedicine(id));
    }

    @GetMapping("/medicines/low-stock")
    public ResponseEntity<List<MedicineResponse>> getLowStock(
            @RequestParam(required = false) Integer stockLessThan) {
        return ResponseEntity.ok(adminMedicineService.getLowStockMedicines(stockLessThan));
    }

    @GetMapping("/medicines/expiring")
    public ResponseEntity<List<MedicineResponse>> getExpiring(
            @RequestParam(required = false) String expiryBefore,
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(adminMedicineService.getExpiringSoon(expiryBefore, days));
    }

    // ── Category endpoints ───────────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(adminMedicineService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.getCategoryById(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMedicineService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(adminMedicineService.updateCategory(id, request));
    }

    @PatchMapping("/categories/{id}/deactivate")
    public ResponseEntity<CategoryResponse> deactivateCategory(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.deactivateCategory(id));
    }

    // ── Prescription endpoints ───────────────────────────────────────

    @GetMapping("/prescriptions/queue")
    public ResponseEntity<PagedResponse<PrescriptionResponse>> getPendingQueue(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminMedicineService.getPendingQueue(userId, page, size));
    }

    @PatchMapping("/prescriptions/{id}/review")
    public ResponseEntity<PrescriptionResponse> reviewPrescription(
            @PathVariable Long id,
            @RequestBody PrescriptionReviewRequest request,
            @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(adminMedicineService.reviewPrescription(id, request, adminId));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<PagedResponse<PrescriptionResponse>> getAllPrescriptions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminMedicineService.getAllPrescriptions(status, userId, page, size));
    }
}
