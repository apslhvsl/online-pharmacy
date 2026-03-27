package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.MedicineCreateRequest;
import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.dto.StockCheckResponse;
import com.pharmacy.catalog.entity.Category;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.mapper.MedicineMapper;
import com.pharmacy.catalog.repository.CategoryRepository;
import com.pharmacy.catalog.repository.InventoryBatchRepository;
import com.pharmacy.catalog.repository.MedicineRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryBatchRepository batchRepository;
    private final InventoryBatchService batchService;
    private final MedicineMapper medicineMapper;

    public Page<MedicineDto> getMedicines(String q, Long categoryId, Boolean requiresPrescription,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Pageable pageable) {
        return medicineRepository.findByFilters(q, categoryId, requiresPrescription, minPrice, maxPrice, pageable)
                .map(m -> enrichWithStock(medicineMapper.toDto(m), m.getId()));
    }

    public MedicineDto getMedicineById(Long id) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));
        return enrichWithStock(medicineMapper.toDto(m), id);
    }

    public StockCheckResponse checkStock(Long medicineId, Integer requestedQuantity) {
        medicineRepository.findById(medicineId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + medicineId));
        int available = batchRepository.sumAvailableStock(medicineId, LocalDate.now());
        return StockCheckResponse.builder()
                .medicineId(medicineId)
                .requestedQuantity(requestedQuantity)
                .availableQuantity(available)
                .available(available >= requestedQuantity)
                .build();
    }

    public List<MedicineDto> getFeaturedMedicines() {
        return medicineRepository.findByIsFeaturedTrueAndActiveTrue()
                .stream().map(m -> enrichWithStock(medicineMapper.toDto(m), m.getId())).toList();
    }

    public List<MedicineDto> getLowStockMedicines(Integer stockLessThan) {
        int threshold = stockLessThan != null ? stockLessThan : 10;
        List<Long> lowStockIds = batchRepository.findMedicineIdsWithLowStock(threshold, LocalDate.now());
        return medicineRepository.findAllById(lowStockIds).stream()
                .filter(Medicine::getActive)
                .map(m -> enrichWithStock(medicineMapper.toDto(m), m.getId()))
                .toList();
    }

    public List<MedicineDto> getExpiringSoon(String expiryBefore, int days) {
        // Delegate to batch service — returns batches; here we return distinct medicines
        LocalDate threshold = expiryBefore != null ? LocalDate.parse(expiryBefore) : LocalDate.now().plusDays(days);
        List<Long> medicineIds = batchRepository.findExpiringSoon(threshold)
                .stream().map(b -> b.getMedicine().getId()).distinct().toList();
        return medicineRepository.findAllById(medicineIds).stream()
                .map(m -> enrichWithStock(medicineMapper.toDto(m), m.getId()))
                .toList();
    }

    @Transactional
    public MedicineDto createMedicine(MedicineCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));

        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .brandName(request.getBrandName())
                .activeIngredient(request.getActiveIngredient())
                .category(category)
                .price(request.getPrice())
                .mrp(request.getMrp())
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 10)
                .requiresPrescription(request.getRequiresPrescription())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .packSize(request.getPackSize())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .manufacturer(request.getManufacturer())
                .isFeatured(request.getIsFeatured() != null && request.getIsFeatured())
                .active(true)
                .build();

        return medicineMapper.toDto(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineDto updateMedicine(Long id, MedicineCreateRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));

        if (request.getName() != null) medicine.setName(request.getName());
        if (request.getBrandName() != null) medicine.setBrandName(request.getBrandName());
        if (request.getActiveIngredient() != null) medicine.setActiveIngredient(request.getActiveIngredient());
        if (request.getPrice() != null) medicine.setPrice(request.getPrice());
        if (request.getMrp() != null) medicine.setMrp(request.getMrp());
        if (request.getReorderLevel() != null) medicine.setReorderLevel(request.getReorderLevel());
        if (request.getRequiresPrescription() != null) medicine.setRequiresPrescription(request.getRequiresPrescription());
        if (request.getDosageForm() != null) medicine.setDosageForm(request.getDosageForm());
        if (request.getStrength() != null) medicine.setStrength(request.getStrength());
        if (request.getPackSize() != null) medicine.setPackSize(request.getPackSize());
        if (request.getDescription() != null) medicine.setDescription(request.getDescription());
        if (request.getImageUrl() != null) medicine.setImageUrl(request.getImageUrl());
        if (request.getManufacturer() != null) medicine.setManufacturer(request.getManufacturer());
        if (request.getIsFeatured() != null) medicine.setIsFeatured(request.getIsFeatured());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
            medicine.setCategory(category);
        }

        return medicineMapper.toDto(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineDto deactivateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));
        medicine.setActive(false);
        return medicineMapper.toDto(medicineRepository.save(medicine));
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    /** Used by Order Service via Feign — FEFO deduction across batches */
    @Transactional
    public void deductStock(Long medicineId, Integer quantity) {
        batchService.deductStock(medicineId, quantity);
    }

    public List<MedicineDto> getAllMedicinesAsList() {
        return medicineRepository.findAll().stream()
                .map(m -> enrichWithStock(medicineMapper.toDto(m), m.getId()))
                .toList();
    }

    /** Admin view — includes inactive medicines, supports full filters */
    public Page<MedicineDto> getMedicinesAdmin(String q, Long categoryId, Boolean requiresPrescription,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               Pageable pageable) {
        return medicineRepository.findByFiltersAdmin(q, categoryId, requiresPrescription, minPrice, maxPrice, pageable)
                .map(m -> enrichWithStock(medicineMapper.toDto(m), m.getId()));
    }

    /** Admin get by id — does not filter by active */
    public MedicineDto getMedicineByIdAdmin(Long id) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));
        return enrichWithStock(medicineMapper.toDto(m), id);
    }

    private MedicineDto enrichWithStock(MedicineDto dto, Long medicineId) {
        dto.setStock(batchRepository.sumAvailableStock(medicineId, LocalDate.now()));
        return dto;
    }
}
