package com.pharmacy.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private BigDecimal price;
}