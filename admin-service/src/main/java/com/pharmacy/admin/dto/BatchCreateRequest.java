package com.pharmacy.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BatchCreateRequest {
    private Long medicineId;
    private String batchNumber;
    private LocalDate expiryDate;
    private BigDecimal price;
    private Integer quantity;
}
