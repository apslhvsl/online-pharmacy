package com.pharmacy.orderservice.dto;

import com.pharmacy.orderservice.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private PaymentMethod paymentMethod;
}
