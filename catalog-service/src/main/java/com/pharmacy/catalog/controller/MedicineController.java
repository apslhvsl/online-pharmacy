package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public-facing medicine endpoints — accessible by anyone through the gateway.
 * Write operations live in InternalMedicineController (Feign-only, not gateway-routed).
 */
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/catalog/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<Page<MedicineDto>> getMedicines(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicineService.getMedicines(q, categoryId, requiresPrescription, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }
}
