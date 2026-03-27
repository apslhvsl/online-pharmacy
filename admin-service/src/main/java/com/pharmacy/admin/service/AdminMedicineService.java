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

    public List<MedicineResponse> getAllMedicines() {
        return catalogClient.getAllMedicines();
    }

    public MedicineResponse getMedicineById(Long id) {
        return catalogClient.getMedicineById(id);
    }

    public MedicineResponse createMedicine(MedicineCreateRequest request, Long adminId) {
        return catalogClient.createMedicine(request);
    }

    public MedicineResponse updateMedicine(Long id, MedicineCreateRequest request) {
        return catalogClient.updateMedicine(id, request);
    }

    public MedicineResponse adjustStock(Long id, StockAdjustRequest request, Long adminId) {
        return catalogClient.adjustStock(id, request, adminId);
    }

    public MedicineResponse deactivateMedicine(Long id) {
        return catalogClient.deactivateMedicine(id);
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
