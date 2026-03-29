package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.dto.PrescriptionReviewRequest;
import com.pharmacy.catalog.entity.PrescriptionStatus;
import com.pharmacy.catalog.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Internal prescription endpoints — NOT gateway-routed.
 * Called exclusively by Admin Service via Feign.
 */
@RestController
@RequestMapping("/api/catalog/internal/prescriptions")
@RequiredArgsConstructor
public class InternalPrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/queue")
    public ResponseEntity<Page<PrescriptionDto>> getPendingQueue(
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getPendingQueue(userId, pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PrescriptionDto> reviewPrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionReviewRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        return ResponseEntity.ok(prescriptionService.reviewPrescription(id, request, adminId));
    }

    @GetMapping
    public ResponseEntity<Page<PrescriptionDto>> getAllPrescriptions(
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions(status, userId, dateFrom, dateTo, pageable));
    }

    // Admin can also download prescription files
    @GetMapping("/{id}/file-path")
    public ResponseEntity<String> getPrescriptionFilePath(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionFilePath(id).toString());
    }

    /** Used by Order Service to validate prescription status at checkout */
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDto> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id, null, true));
    }
}
