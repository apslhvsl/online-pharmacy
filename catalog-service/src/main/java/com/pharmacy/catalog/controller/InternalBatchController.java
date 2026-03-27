package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.*;
import com.pharmacy.catalog.service.InventoryBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal batch endpoints — NOT gateway-routed.
 * Called by Admin Service (CRUD) and Order Service (stock-check / deduct) via Feign.
 */
@RestController
@RequestMapping("/api/catalog/internal/batches")
@RequiredArgsConstructor
public class InternalBatchController {

    private final InventoryBatchService batchService;

    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<InventoryBatchDto>> getBatchesForMedicine(@PathVariable Long medicineId) {
        return ResponseEntity.ok(batchService.getBatchesForMedicine(medicineId));
    }

    @PostMapping
    public ResponseEntity<InventoryBatchDto> createBatch(@Valid @RequestBody InventoryBatchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(request));
    }

    @PutMapping("/{batchId}")
    public ResponseEntity<InventoryBatchDto> updateBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody InventoryBatchCreateRequest request) {
        return ResponseEntity.ok(batchService.updateBatch(batchId, request));
    }

    @PatchMapping("/{batchId}/stock")
    public ResponseEntity<InventoryBatchDto> adjustStock(
            @PathVariable Long batchId,
            @Valid @RequestBody StockAdjustRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long performedBy) {
        return ResponseEntity.ok(batchService.adjustBatchStock(batchId, request, performedBy));
    }

    @PostMapping("/{batchId}/deduct")
    public ResponseEntity<Void> deductBatchStock(
            @PathVariable Long batchId,
            @RequestParam Integer quantity) {
        batchService.deductBatchStock(batchId, quantity);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{batchId}/stock-check")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable Long batchId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(batchService.checkStock(batchId, quantity));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<InventoryBatchDto>> getExpiringSoon(
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(batchService.getExpiringSoon(days));
    }
}
