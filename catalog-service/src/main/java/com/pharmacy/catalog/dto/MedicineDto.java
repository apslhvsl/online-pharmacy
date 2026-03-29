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
    /** Computed: sum of non-expired batch quantities */
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
