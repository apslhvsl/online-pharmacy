package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineCreateRequest;
import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "List all medicines (admin)", description = "Returns a paginated list of all medicines including inactive ones, with optional filters. For internal use by Admin Service only.")
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

    @Operation(summary = "Get medicine by ID (admin)", description = "Returns the full details of a medicine by ID, including inactive ones. For internal use by Admin Service only.")
    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineByIdAdmin(id));
    }

    @Operation(summary = "Create a new medicine", description = "Creates a new medicine entry in the catalog. For internal use by Admin Service only.")
    @PostMapping
    public ResponseEntity<MedicineDto> createMedicine(@Valid @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.createMedicine(request));
    }

    @Operation(summary = "Update a medicine", description = "Replaces all fields of an existing medicine with the provided data. For internal use by Admin Service only.")
    @PutMapping("/{id}")
    public ResponseEntity<MedicineDto> updateMedicine(
            @PathVariable Long id,
            @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, request));
    }

    @Operation(summary = "Deactivate a medicine", description = "Marks a medicine as inactive so it no longer appears in the public catalog. For internal use by Admin Service only.")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MedicineDto> deactivateMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.deactivateMedicine(id));
    }

    @Operation(summary = "Get low-stock medicines", description = "Returns medicines whose total stock is below the specified threshold. For internal use by Admin Service only.")
    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicineDto>> getLowStock(
            @RequestParam(required = false) Integer stockLessThan) {
        return ResponseEntity.ok(medicineService.getLowStockMedicines(stockLessThan));
    }

    @Operation(summary = "Get medicines expiring soon", description = "Returns medicines with batches expiring before the given date or within the specified number of days. For internal use by Admin Service only.")
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<MedicineDto>> getExpiringSoon(
            @RequestParam(required = false) String expiryBefore,
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(medicineService.getExpiringSoon(expiryBefore, days));
    }
}
