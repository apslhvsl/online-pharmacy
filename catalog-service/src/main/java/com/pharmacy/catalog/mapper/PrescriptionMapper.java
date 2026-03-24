package com.pharmacy.catalog.mapper;

import com.pharmacy.catalog.dto.PrescriptionDto;
import com.pharmacy.catalog.entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    PrescriptionDto toDto(Prescription prescription);
}