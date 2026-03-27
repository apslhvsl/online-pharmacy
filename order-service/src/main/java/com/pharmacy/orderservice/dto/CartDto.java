package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long userId;
    private List<CartItemDto> items;
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private Boolean requiresPrescription;
}
