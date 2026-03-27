package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCreateRequest {
    private String name;
    private String brandName;
    private String activeIngredient;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer stock;
    private Integer reorderLevel;
    private Boolean requiresPrescription;
    private String dosageForm;
    private String strength;
    private String packSize;
    private String description;
    private String imageUrl;
    private LocalDate expiryDate;
    private String manufacturer;
    private Boolean isFeatured;
}
