package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.entity.PrescriptionStatus;
import com.pharmacy.catalog.exception.InvalidFileTypeException;
import com.pharmacy.catalog.mapper.PrescriptionMapper;
import com.pharmacy.catalog.repository.PrescriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
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

    public PrescriptionDto uploadPrescription(MultipartFile file, Long userId) throws IOException {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException("Only PDF, JPG, and PNG files are allowed.");
        }

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Prescription prescription = Prescription.builder()
                .userId(userId)
                .filePath(filePath.toString())
                .originalFileName(file.getOriginalFilename())
                .status(PrescriptionStatus.PENDING)
                .build();

        return prescriptionMapper.toDto(prescriptionRepository.save(prescription));
    }

    public PrescriptionDto getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .map(prescriptionMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id: " + id));
    }

    public PrescriptionStatus getPrescriptionStatus(Long id) {
        return prescriptionRepository.findById(id)
                .map(Prescription::getStatus)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id: " + id));
    }
}