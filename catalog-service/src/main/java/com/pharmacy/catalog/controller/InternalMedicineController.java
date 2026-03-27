package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineCreateRequest;
import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal medicine endpoints — NOT gateway-routed.
 * Called exclusively by Admin Service via Feign.
 * Stock operations (adjust, deduct, low-stock, expiring-soon) live in InternalBatchController.
 */
@RestController
@RequestMapping("/api/catalog/internal/medicines")
@RequiredArgsConstructor
public class InternalMedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<List<MedicineDto>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicinesAsList());
    }

    @PostMapping
    public ResponseEntity<MedicineDto> createMedicine(@Valid @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.createMedicine(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineDto> updateMedicine(
            @PathVariable Long id,
            @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MedicineDto> deactivateMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.deactivateMedicine(id));
    }

    /** Used by Order Service to deduct stock on order confirmation (FEFO across batches) */
    @PostMapping("/{id}/deduct")
    public ResponseEntity<Void> deductStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        medicineService.deductStock(id, quantity);
        return ResponseEntity.noContent().build();
    }
}
