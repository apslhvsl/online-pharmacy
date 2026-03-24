package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.dto.StockCheckResponse;
import com.pharmacy.catalog.mapper.MedicineMapper;
import com.pharmacy.catalog.repository.MedicineRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;

    public Page<MedicineDto> getMedicines(String name, Long categoryId, Pageable pageable) {
        return medicineRepository.findByFilters(name, categoryId, pageable)
                .map(medicineMapper::toDto);
    }

    public MedicineDto getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .map(medicineMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + id));
    }

    public StockCheckResponse checkStock(Long medicineId, Integer requestedQuantity) {
        var medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + medicineId));

        return StockCheckResponse.builder()
                .medicineId(medicineId)
                .requestedQuantity(requestedQuantity)
                .availableQuantity(medicine.getStockQuantity())
                .sufficient(medicine.getStockQuantity() >= requestedQuantity)
                .build();
    }
}