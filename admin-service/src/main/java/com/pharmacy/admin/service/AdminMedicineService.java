package com.pharmacy.admin.service;

import com.pharmacy.admin.client.CatalogClient;
import com.pharmacy.admin.dto.MedicineCreateRequest;
import com.pharmacy.admin.dto.MedicineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMedicineService {

    private final CatalogClient catalogClient;

    public MedicineResponse createMedicine(MedicineCreateRequest request) {
        return catalogClient.createMedicine(request);
    }

    public MedicineResponse updateMedicine(Long id, MedicineCreateRequest request) {
        return catalogClient.updateMedicine(id, request);
    }

    public List<MedicineResponse> getAllMedicines() {
        return catalogClient.getAllMedicines();
    }

    public MedicineResponse getMedicineById(Long id) {
        return catalogClient.getMedicineById(id);
    }

    public void updatePrescriptionStatus(Long id, String status) {
        catalogClient.updatePrescriptionStatus(id, status);
    }
}