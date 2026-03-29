package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

    @Operation(
            summary = "Upload a prescription file",
            description = "Uploads an image or PDF prescription for a user"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrescriptionDto> upload(
            @Parameter(description = "Prescription image or PDF", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) throws IOException {
        return ResponseEntity.status(201).body(prescriptionService.uploadPrescription(file, userId));
    }

    @Operation(summary = "List my prescriptions", description = "Returns all prescriptions uploaded by the currently authenticated user")
    @GetMapping
    public ResponseEntity<List<PrescriptionDto>> getMyPrescriptions(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsForUser(userId));
    }

    @Operation(summary = "Get prescription by ID", description = "Returns the details of a specific prescription belonging to the authenticated user")
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDto> getById(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id, userId, false));
    }

    @Operation(summary = "Download prescription file", description = "Streams the original prescription file (image or PDF) for the authenticated user's prescription")
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) throws IOException {
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
