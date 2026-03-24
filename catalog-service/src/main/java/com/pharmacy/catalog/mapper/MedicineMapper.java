package com.pharmacy.catalog.mapper;

import com.pharmacy.catalog.dto.MedicineDto;
import com.pharmacy.catalog.entity.Medicine;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MedicineMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    MedicineDto toDto(Medicine medicine);
}