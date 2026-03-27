package com.pharmacy.orderservice.dto;

import com.pharmacy.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatus status;

    private String note;
}
