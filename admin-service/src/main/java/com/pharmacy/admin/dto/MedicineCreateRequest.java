package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCreateRequest {
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private Boolean requiresPrescription;
    private String manufacturer;
    private String strength;
    private String packSize;
    private String description;
    private String imageUrl;
    private Integer reorderLevel;
}
