package com.pharmacy.catalog.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineDto {
    private Long id;
    private String name;
    private String brandName;
    private String activeIngredient;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal mrp;
    /** Computed: sum of non-expired batch quantities */
    private Integer stock;
    private Integer reorderLevel;
    private Boolean requiresPrescription;
    private String dosageForm;
    private String strength;
    private String packSize;
    private String description;
    private String imageUrl;
    private String manufacturer;
    private Boolean isFeatured;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
