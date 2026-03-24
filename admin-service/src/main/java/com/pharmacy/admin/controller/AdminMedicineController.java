package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.MedicineCreateRequest;
import com.pharmacy.admin.dto.MedicineResponse;
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

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(adminMedicineService.getAllMedicines());
    }

    @GetMapping("/medicines/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(
            @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminMedicineService.getMedicineById(id));
    }

    @PostMapping("/medicines")
    public ResponseEntity<MedicineResponse> createMedicine(
            @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminMedicineService.createMedicine(request));
    }

    @PutMapping("/medicines/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable(name = "id") Long id,
            @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.ok(adminMedicineService.updateMedicine(id, request));
    }

    @PutMapping("/prescriptions/{id}/status")
    public ResponseEntity<Void> updatePrescriptionStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") String status) {
        adminMedicineService.updatePrescriptionStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}