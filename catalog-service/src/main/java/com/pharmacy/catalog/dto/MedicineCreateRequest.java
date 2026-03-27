package com.pharmacy.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCreateRequest {

    @NotBlank
    private String name;
    private String brandName;
    private String activeIngredient;

    @NotNull
    private Long categoryId;

    @NotNull @Positive
    private BigDecimal price;
    private BigDecimal mrp;

    private Integer reorderLevel;

    @NotNull
    private Boolean requiresPrescription;

    private String dosageForm;
    private String strength;
    private String packSize;
    private String description;
    private String imageUrl;
    private String manufacturer;
    private Boolean isFeatured;
}
