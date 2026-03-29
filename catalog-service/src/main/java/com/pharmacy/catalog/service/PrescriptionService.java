package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.dto.PrescriptionReviewRequest;
import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.entity.PrescriptionStatus;
import com.pharmacy.catalog.exception.InvalidFileTypeException;
import com.pharmacy.catalog.mapper.PrescriptionMapper;
import com.pharmacy.catalog.repository.PrescriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;

    @Value("${prescription.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/png"
    );

    @Transactional
    public PrescriptionDto uploadPrescription(MultipartFile file, Long userId) throws IOException {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException("Only PDF, JPG, and PNG files are allowed.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new InvalidFileTypeException("File size must not exceed 5MB.");
        }

        Path userDir = Paths.get(uploadDir, String.valueOf(userId));
        Files.createDirectories(userDir);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = userDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Prescription prescription = Prescription.builder()
                .userId(userId)
                .fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .status(PrescriptionStatus.PENDING)
                .build();

        return prescriptionMapper.toDto(prescriptionRepository.save(prescription));
    }

    public List<PrescriptionDto> getPrescriptionsForUser(Long userId) {
        return prescriptionRepository.findByUserId(userId)
                .stream().map(prescriptionMapper::toDto).toList();
    }

    public PrescriptionDto getPrescriptionById(Long id, Long requestingUserId, boolean isAdmin) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + id));
        if (!isAdmin && !p.getUserId().equals(requestingUserId)) {
            throw new SecurityException("Access denied");
        }
        return prescriptionMapper.toDto(p);
    }

    public Path getPrescriptionFilePath(Long id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + id));
        return Paths.get(p.getFilePath());
    }

    public PrescriptionStatus getPrescriptionStatus(Long id) {
        return prescriptionRepository.findById(id)
                .map(Prescription::getStatus)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + id));
    }

    public Page<PrescriptionDto> getPendingQueue(Long userId, Pageable pageable) {
        return prescriptionRepository.findWithFilters(PrescriptionStatus.PENDING, userId, null, null, pageable)
                .map(prescriptionMapper::toDto);
    }

    @Transactional
    public PrescriptionDto reviewPrescription(Long id, PrescriptionReviewRequest request, Long adminId) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found: " + id));

        if (request.getStatus() != PrescriptionStatus.APPROVED
                && request.getStatus() != PrescriptionStatus.REJECTED) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED");
        }

        p.setStatus(request.getStatus());
        p.setRemarks(request.getRemarks());
        p.setReviewedBy(adminId);
        p.setReviewedAt(LocalDateTime.now());
        return prescriptionMapper.toDto(prescriptionRepository.save(p));
    }

    public Page<PrescriptionDto> getAllPrescriptions(PrescriptionStatus status, Long userId,
                                                     LocalDateTime dateFrom, LocalDateTime dateTo,
                                                     Pageable pageable) {
        return prescriptionRepository.findWithFilters(status, userId, dateFrom, dateTo, pageable)
                .map(prescriptionMapper::toDto);
    }
}
