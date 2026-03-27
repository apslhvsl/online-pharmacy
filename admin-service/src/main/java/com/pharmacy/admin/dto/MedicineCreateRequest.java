package com.pharmacy.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCreateRequest {
    private String name;
    private String description;
    private Long categoryId;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean requiresPrescription;
    private LocalDate expiryDate;
}