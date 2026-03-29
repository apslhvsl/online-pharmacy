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
    private LocalDate expiryDate;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
