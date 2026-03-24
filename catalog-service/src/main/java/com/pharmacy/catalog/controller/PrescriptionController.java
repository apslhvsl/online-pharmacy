package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.entity.PrescriptionStatus;
import com.pharmacy.catalog.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/catalog/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/upload")
    public ResponseEntity<PrescriptionDto> upload(
            @RequestParam(name = "file") MultipartFile file,
            @RequestHeader(name = "X-User-Id") Long userId) throws IOException {
        return ResponseEntity.status(201).body(prescriptionService.uploadPrescription(file, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDto> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<PrescriptionStatus> getStatus(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionStatus(id));
    }
}