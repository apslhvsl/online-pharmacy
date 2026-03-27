package com.pharmacy.catalog.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer quantity;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
