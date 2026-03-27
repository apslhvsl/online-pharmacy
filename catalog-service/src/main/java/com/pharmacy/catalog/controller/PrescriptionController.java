package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Customer-facing prescription endpoints — accessible by authenticated customers through the gateway.
 * Admin operations live in InternalPrescriptionController (Feign-only, not gateway-routed).
 */
@RestController
@RequestMapping("/api/catalog/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/upload")
    public ResponseEntity<PrescriptionDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        return ResponseEntity.status(201).body(prescriptionService.uploadPrescription(file, userId));
    }

    @GetMapping
    public ResponseEntity<List<PrescriptionDto>> getMyPrescriptions(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDto> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id, userId, false));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        // ownership enforced in service
        prescriptionService.getPrescriptionById(id, userId, false);

        Path filePath = prescriptionService.getPrescriptionFilePath(id);
        Resource resource = new PathResource(filePath);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}
