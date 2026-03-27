package com.pharmacy.admin.client;

import com.pharmacy.admin.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    // ── Public reads (used by dashboard/reports) ─────────────────────

    @GetMapping("/api/catalog/medicines/{id}")
    MedicineResponse getMedicineById(@PathVariable("id") Long id);

    @GetMapping("/api/catalog/categories")
    List<CategoryResponse> getAllCategories();

    @GetMapping("/api/catalog/categories/{id}")
    CategoryResponse getCategoryById(@PathVariable("id") Long id);

    // ── Internal medicine operations ─────────────────────────────────

    @GetMapping("/api/catalog/internal/medicines")
    List<MedicineResponse> getAllMedicines();

    @PostMapping("/api/catalog/internal/medicines")
    MedicineResponse createMedicine(@RequestBody MedicineCreateRequest request);

    @PutMapping("/api/catalog/internal/medicines/{id}")
    MedicineResponse updateMedicine(@PathVariable("id") Long id, @RequestBody MedicineCreateRequest request);

    @PatchMapping("/api/catalog/internal/medicines/{id}/stock")
    MedicineResponse adjustStock(@PathVariable("id") Long id,
                                 @RequestBody StockAdjustRequest request,
                                 @RequestHeader("X-User-Id") Long performedBy);

    @PatchMapping("/api/catalog/internal/medicines/{id}/deactivate")
    MedicineResponse deactivateMedicine(@PathVariable("id") Long id);

    @GetMapping("/api/catalog/internal/medicines/low-stock")
    List<MedicineResponse> getLowStockMedicines(@RequestParam(required = false) Integer stockLessThan);

    @GetMapping("/api/catalog/internal/medicines/expiring-soon")
    List<MedicineResponse> getExpiringSoon(
            @RequestParam(required = false) String expiryBefore,
            @RequestParam(defaultValue = "90") int days);

    // ── Internal category operations ─────────────────────────────────

    @PostMapping("/api/catalog/internal/categories")
    CategoryResponse createCategory(@RequestBody CategoryCreateRequest request);

    @PutMapping("/api/catalog/internal/categories/{id}")
    CategoryResponse updateCategory(@PathVariable("id") Long id, @RequestBody CategoryCreateRequest request);

    @PatchMapping("/api/catalog/internal/categories/{id}/deactivate")
    CategoryResponse deactivateCategory(@PathVariable("id") Long id);

    // ── Internal prescription operations ─────────────────────────────

    @GetMapping("/api/catalog/internal/prescriptions/queue")
    PagedResponse<PrescriptionResponse> getPendingQueue(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @PatchMapping("/api/catalog/internal/prescriptions/{id}/status")
    PrescriptionResponse reviewPrescription(@PathVariable("id") Long id,
                                            @RequestBody PrescriptionReviewRequest request,
                                            @RequestHeader("X-User-Id") Long adminId);

    @GetMapping("/api/catalog/internal/prescriptions")
    PagedResponse<PrescriptionResponse> getAllPrescriptions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);
}
