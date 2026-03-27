package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private String name;
    private String brandName;
    private String activeIngredient;
    private Long categoryId;
    private String categoryName;
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
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
