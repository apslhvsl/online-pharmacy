package com.pharmacy.orderservice.dto;

import com.pharmacy.orderservice.entity.OrderStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionDto {
    private Long orderId;
    private OrderStatus status;
    private Boolean requiresPrescription;
    private String prescriptionStatus;
}
