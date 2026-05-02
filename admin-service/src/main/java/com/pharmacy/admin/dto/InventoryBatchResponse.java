package com.pharmacy.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryBatchResponse {
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
