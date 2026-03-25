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

import com.pharmacy.catalog.entity.Category;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;

    private final CategoryRepository categoryRepository;

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
    @Transactional
    public MedicineDto createMedicine(MedicineDto request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));

        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .category(category)
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .requiresPrescription(request.getRequiresPrescription())
                .expiryDate(request.getExpiryDate())
                .build();

        return medicineMapper.toDto(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineDto updateMedicine(Long id, MedicineDto request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + id));

        if (request.getName() != null) medicine.setName(request.getName());
        if (request.getPrice() != null) medicine.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) medicine.setStockQuantity(request.getStockQuantity());
        if (request.getRequiresPrescription() != null) medicine.setRequiresPrescription(request.getRequiresPrescription());
        if (request.getExpiryDate() != null) medicine.setExpiryDate(request.getExpiryDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
            medicine.setCategory(category);
        }

        return medicineMapper.toDto(medicineRepository.save(medicine));
    }
}