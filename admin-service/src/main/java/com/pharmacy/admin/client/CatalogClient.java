package com.pharmacy.admin.client;

import com.pharmacy.admin.dto.MedicineCreateRequest;
import com.pharmacy.admin.dto.MedicineResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.pharmacy.admin.dto.CategoryResponse;
import java.util.List;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @PostMapping("/api/catalog/medicines")
    MedicineResponse createMedicine(@RequestBody MedicineCreateRequest request);

    @PutMapping("/api/catalog/medicines/{id}")
    MedicineResponse updateMedicine(
            @PathVariable(name = "id") Long id,
            @RequestBody MedicineCreateRequest request);

    @GetMapping("/api/catalog/medicines/{id}")
    MedicineResponse getMedicineById(@PathVariable(name = "id") Long id);

    @GetMapping("/api/catalog/medicines/all")
    List<MedicineResponse> getAllMedicines();

    @GetMapping("/api/catalog/categories")
    List<CategoryResponse> getAllCategories();

    @PutMapping("/api/catalog/prescriptions/{id}/status")
    void updatePrescriptionStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") String status);
}