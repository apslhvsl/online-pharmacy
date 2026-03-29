package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private Boolean active;
    private Boolean requiresPrescription;
    private String manufacturer;
    private String strength;
    private String packSize;
    private String description;
    private String imageUrl;
    private Integer reorderLevel;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
