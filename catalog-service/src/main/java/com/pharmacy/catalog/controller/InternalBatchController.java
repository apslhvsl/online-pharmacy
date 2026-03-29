package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.*;
import com.pharmacy.catalog.service.InventoryBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Get batches for a medicine", description = "Returns all inventory batches associated with the specified medicine. For internal use only.")
    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<InventoryBatchDto>> getBatchesForMedicine(@PathVariable Long medicineId) {
        return ResponseEntity.ok(batchService.getBatchesForMedicine(medicineId));
    }

    @Operation(summary = "Create a new inventory batch", description = "Adds a new stock batch for a medicine, including quantity, expiry date, and cost price. For internal use only.")
    @PostMapping
    public ResponseEntity<InventoryBatchDto> createBatch(@Valid @RequestBody InventoryBatchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(request));
    }

    @Operation(summary = "Update an inventory batch", description = "Replaces all fields of an existing inventory batch with the provided data. For internal use only.")
    @PutMapping("/{batchId}")
    public ResponseEntity<InventoryBatchDto> updateBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody InventoryBatchCreateRequest request) {
        return ResponseEntity.ok(batchService.updateBatch(batchId, request));
    }

    @Operation(summary = "Adjust batch stock", description = "Manually increases or decreases the stock quantity of a batch and records the adjustment. For internal use only.")
    @PatchMapping("/{batchId}/stock")
    public ResponseEntity<InventoryBatchDto> adjustStock(
            @PathVariable Long batchId,
            @Valid @RequestBody StockAdjustRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long performedBy) {
        return ResponseEntity.ok(batchService.adjustBatchStock(batchId, request, performedBy));
    }

    @Operation(summary = "Deduct stock from a batch", description = "Reduces the stock of a batch by the specified quantity. Used by Order Service during order fulfilment.")
    @PostMapping("/{batchId}/deduct")
    public ResponseEntity<Void> deductBatchStock(
            @PathVariable Long batchId,
            @RequestParam Integer quantity) {
        batchService.deductBatchStock(batchId, quantity);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check batch stock availability", description = "Verifies whether a batch has sufficient stock for the requested quantity. Used by Order Service before checkout confirmation.")
    @GetMapping("/{batchId}/stock-check")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable Long batchId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(batchService.checkStock(batchId, quantity));
    }
}
