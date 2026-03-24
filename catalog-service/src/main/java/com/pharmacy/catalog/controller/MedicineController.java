package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.dto.StockCheckResponse;
import com.pharmacy.catalog.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<Page<MedicineDto>> getMedicines(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicineService.getMedicines(name, categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @GetMapping("/{id}/stock-check")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "quantity") Integer quantity) {
        return ResponseEntity.ok(medicineService.checkStock(id, quantity));
    }
}