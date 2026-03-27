package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.StockAdjustRequest;
import com.pharmacy.admin.service.AdminMedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/batches")
@RequiredArgsConstructor
public class AdminBatchController {

    private final AdminMedicineService adminMedicineService;

    /** PATCH /api/admin/batches/{batchId}/stock — adjust stock for a specific batch */
    @PatchMapping("/{batchId}/stock")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Long batchId,
            @RequestBody StockAdjustRequest request,
            @RequestHeader("X-User-Id") Long adminId) {
        request.setBatchId(batchId);
        adminMedicineService.adjustStock(batchId, request, adminId);
        return ResponseEntity.noContent().build();
    }
}
