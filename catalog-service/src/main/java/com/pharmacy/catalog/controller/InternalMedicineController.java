package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineCreateRequest;
import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Internal medicine endpoints — NOT gateway-routed.
 * Called exclusively by Admin Service via Feign.
 */
@RestController
@RequestMapping("/api/catalog/internal/medicines")
@RequiredArgsConstructor
public class InternalMedicineController {

    private final MedicineService medicineService;

    /** Admin-filtered list — includes inactive medicines */
    @GetMapping
    public ResponseEntity<Page<MedicineDto>> getAllMedicines(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicineService.getMedicinesAdmin(q, categoryId, requiresPrescription, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineByIdAdmin(id));
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

    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicineDto>> getLowStock(
            @RequestParam(required = false) Integer stockLessThan) {
        return ResponseEntity.ok(medicineService.getLowStockMedicines(stockLessThan));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<MedicineDto>> getExpiringSoon(
            @RequestParam(required = false) String expiryBefore,
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(medicineService.getExpiringSoon(expiryBefore, days));
    }
}
