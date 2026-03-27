package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long batchId;
    private Long medicineId;
    private String medicineName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Boolean requiresPrescription;
    private BigDecimal lineTotal;
}
