package com.pharmacy.admin.service;

import com.pharmacy.admin.client.CatalogClient;
import com.pharmacy.admin.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMedicineService {

    private final CatalogClient catalogClient;

    public PagedResponse<MedicineResponse> getAllMedicines(String q, Long categoryId, Boolean requiresPrescription,
                                                  Boolean inStock, int page, int size) {
        return catalogClient.getAllMedicines(q, categoryId, requiresPrescription, inStock, page, size);
    }

    public MedicineResponse getMedicineById(Long id) {
        return catalogClient.getMedicineByIdInternal(id);
    }

    public MedicineResponse createMedicine(MedicineCreateRequest request, Long adminId) {
        return catalogClient.createMedicine(request);
    }

    public MedicineResponse updateMedicine(Long id, MedicineCreateRequest request) {
        return catalogClient.updateMedicine(id, request);
    }

    public MedicineResponse deactivateMedicine(Long id) {
        return catalogClient.deactivateMedicine(id);
    }

    public void adjustStock(Long medicineId, StockAdjustRequest request, Long adminId) {
        // batchId is mandatory — we don't want to accidentally adjust the wrong batch
        if (request.getBatchId() == null) throw new IllegalArgumentException("batchId is required for stock adjustment");
        catalogClient.adjustBatchStock(request.getBatchId(), request, adminId);
    }

    public List<MedicineResponse> getLowStockMedicines(Integer stockLessThan) {
        return catalogClient.getLowStockMedicines(stockLessThan);
    }

    public List<MedicineResponse> getExpiringSoon(String expiryBefore, int days) {
        return catalogClient.getExpiringSoon(expiryBefore, days);
    }

    // ── Categories ───────────────────────────────────────────────────

    public List<CategoryResponse> getAllCategories() {
        return catalogClient.getAllCategories();
    }

    public CategoryResponse getCategoryById(Long id) {
        return catalogClient.getCategoryById(id);
    }

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        return catalogClient.createCategory(request);
    }

    public CategoryResponse updateCategory(Long id, CategoryCreateRequest request) {
        return catalogClient.updateCategory(id, request);
    }

    public CategoryResponse deactivateCategory(Long id) {
        return catalogClient.deactivateCategory(id);
    }

    // ── Prescriptions ────────────────────────────────────────────────

    public PagedResponse<PrescriptionResponse> getPendingQueue(Long userId, int page, int size) {
        return catalogClient.getPendingQueue(userId, page, size);
    }

    public PrescriptionResponse reviewPrescription(Long id, PrescriptionReviewRequest request, Long adminId) {
        return catalogClient.reviewPrescription(id, request, adminId);
    }

    public PagedResponse<PrescriptionResponse> getAllPrescriptions(String status, Long userId, int page, int size) {
        return catalogClient.getAllPrescriptions(status, userId, page, size);
    }
}
