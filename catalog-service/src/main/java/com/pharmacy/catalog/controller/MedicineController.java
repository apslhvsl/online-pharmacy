package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Search and list medicines", description = "Returns a paginated list of active medicines, with optional filters for name, category, prescription requirement, and price range")
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

    @Operation(summary = "Get medicine by ID", description = "Returns the details of a single active medicine by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @Operation(summary = "Check medicine stock", description = "Returns stock availability and the best batch ID for a given medicine and requested quantity")
    @GetMapping("/{id}/stock-check")
    public ResponseEntity<com.pharmacy.catalog.dto.StockCheckResponse> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(medicineService.checkStock(id, quantity));
    }
}
