package com.pharmacy.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}