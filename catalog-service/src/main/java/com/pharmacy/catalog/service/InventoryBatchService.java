package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.InventoryBatchCreateRequest;
import com.pharmacy.catalog.dto.InventoryBatchDto;
import com.pharmacy.catalog.dto.StockAdjustRequest;
import com.pharmacy.catalog.dto.StockCheckResponse;
import com.pharmacy.catalog.entity.InventoryAudit;
import com.pharmacy.catalog.entity.InventoryBatch;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.repository.InventoryAuditRepository;
import com.pharmacy.catalog.repository.InventoryBatchRepository;
import com.pharmacy.catalog.repository.MedicineRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryBatchService {

    private final InventoryBatchRepository batchRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryAuditRepository auditRepository;

    public List<InventoryBatchDto> getBatchesForMedicine(Long medicineId) {
        return batchRepository.findByMedicineId(medicineId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public InventoryBatchDto createBatch(InventoryBatchCreateRequest request) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + request.getMedicineId()));

        InventoryBatch batch = InventoryBatch.builder()
                .medicine(medicine)
                .batchNumber(request.getBatchNumber())
                .expiryDate(request.getExpiryDate())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        return toDto(batchRepository.save(batch));
    }

    @Transactional
    public InventoryBatchDto updateBatch(Long batchId, InventoryBatchCreateRequest request) {
        InventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found: " + batchId));
        if (request.getBatchNumber() != null) batch.setBatchNumber(request.getBatchNumber());
        if (request.getExpiryDate() != null) batch.setExpiryDate(request.getExpiryDate());
        if (request.getPrice() != null) batch.setPrice(request.getPrice());
        if (request.getQuantity() != null) batch.setQuantity(request.getQuantity());
        return toDto(batchRepository.save(batch));
    }

    @Transactional
    public InventoryBatchDto adjustBatchStock(Long batchId, StockAdjustRequest request, Long performedBy) {
        InventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found: " + batchId));

        int before = batch.getQuantity();
        int after = before + request.getAdjustment();
        if (after < 0) throw new IllegalStateException("Stock cannot go below zero");

        batch.setQuantity(after);
        batchRepository.save(batch);

        auditRepository.save(InventoryAudit.builder()
                .batch(batch)
                .adjustment(request.getAdjustment())
                .stockBefore(before)
                .stockAfter(after)
                .reason(request.getReason())
                .performedBy(performedBy)
                .build());

        return toDto(batch);
    }

    public StockCheckResponse checkStock(Long batchId, Integer requestedQty) {
        InventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found: " + batchId));
        return StockCheckResponse.builder()
                .medicineId(batch.getMedicine().getId())
                .batchId(batchId)
                .requestedQuantity(requestedQty)
                .availableQuantity(batch.getQuantity())
                .available(batch.getQuantity() >= requestedQty && batch.getExpiryDate().isAfter(LocalDate.now()))
                .build();
    }

    @Transactional
    public void deductStock(Long medicineId, Integer quantity) {
        List<InventoryBatch> batches = batchRepository.findAvailableByMedicineId(medicineId, LocalDate.now());
        int remaining = quantity;
        for (InventoryBatch batch : batches) {
            if (remaining <= 0) break;
            int deduct = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - deduct);
            batchRepository.save(batch);
            remaining -= deduct;
        }
        if (remaining > 0) throw new IllegalStateException("Insufficient stock for medicine: " + medicineId);
    }

    @Transactional
    public void deductBatchStock(Long batchId, Integer quantity) {
        InventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found: " + batchId));
        if (batch.getQuantity() < quantity)
            throw new IllegalStateException("Insufficient stock in batch: " + batchId);
        batch.setQuantity(batch.getQuantity() - quantity);
        batchRepository.save(batch);
    }

    public Integer getTotalAvailableStock(Long medicineId) {
        return batchRepository.sumAvailableStock(medicineId, LocalDate.now());
    }

    public List<InventoryBatchDto> getExpiringSoon(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return batchRepository.findExpiringSoon(threshold)
                .stream().map(this::toDto).toList();
    }

    private InventoryBatchDto toDto(InventoryBatch b) {
        return InventoryBatchDto.builder()
                .id(b.getId())
                .medicineId(b.getMedicine().getId())
                .medicineName(b.getMedicine().getName())
                .batchNumber(b.getBatchNumber())
                .expiryDate(b.getExpiryDate())
                .price(b.getPrice())
                .quantity(b.getQuantity())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
