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

        // store under a per-user directory to keep things organised
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
        if (userId != null) {
            return prescriptionRepository.findByStatusAndUserId(PrescriptionStatus.PENDING, userId, pageable)
                    .map(prescriptionMapper::toDto);
        }
        return prescriptionRepository.findByStatus(PrescriptionStatus.PENDING, pageable)
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
        return dispatchPrescriptionQuery(status, userId, dateFrom, dateTo, pageable)
                .map(prescriptionMapper::toDto);
    }

    private Page<Prescription> dispatchPrescriptionQuery(PrescriptionStatus status, Long userId,
                                                          LocalDateTime dateFrom, LocalDateTime dateTo,
                                                          Pageable pageable) {
        boolean hasStatus = status != null;
        boolean hasUser   = userId != null;
        boolean hasFrom   = dateFrom != null;
        boolean hasTo     = dateTo != null;

        if (hasStatus && hasUser && hasFrom && hasTo) return prescriptionRepository.findByStatusAndUserIdAndDateRange(status, userId, dateFrom, dateTo, pageable);
        if (hasStatus && hasUser && hasFrom)          return prescriptionRepository.findByStatusAndUserIdAndDateFrom(status, userId, dateFrom, pageable);
        if (hasStatus && hasUser && hasTo)            return prescriptionRepository.findByStatusAndUserIdAndDateTo(status, userId, dateTo, pageable);
        if (hasStatus && hasUser)                     return prescriptionRepository.findByStatusAndUserId(status, userId, pageable);
        if (hasStatus && hasFrom && hasTo)            return prescriptionRepository.findByStatusAndDateRange(status, dateFrom, dateTo, pageable);
        if (hasStatus && hasFrom)                     return prescriptionRepository.findByStatusAndDateFrom(status, dateFrom, pageable);
        if (hasStatus && hasTo)                       return prescriptionRepository.findByStatusAndDateTo(status, dateTo, pageable);
        if (hasStatus)                                return prescriptionRepository.findByStatus(status, pageable);
        if (hasUser && hasFrom && hasTo)              return prescriptionRepository.findByUserIdAndDateRange(userId, dateFrom, dateTo, pageable);
        if (hasUser && hasFrom)                       return prescriptionRepository.findByUserIdAndDateFrom(userId, dateFrom, pageable);
        if (hasUser && hasTo)                         return prescriptionRepository.findByUserIdAndDateTo(userId, dateTo, pageable);
        if (hasUser)                                  return prescriptionRepository.findByUserId(userId, pageable);
        if (hasFrom && hasTo)                         return prescriptionRepository.findByDateRange(dateFrom, dateTo, pageable);
        if (hasFrom)                                  return prescriptionRepository.findByDateFrom(dateFrom, pageable);
        if (hasTo)                                    return prescriptionRepository.findByDateTo(dateTo, pageable);
        return prescriptionRepository.findAll(pageable);
    }
}
