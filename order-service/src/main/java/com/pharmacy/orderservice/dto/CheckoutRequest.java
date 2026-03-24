package com.pharmacy.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private Long prescriptionId;

    private String idempotencyKey;
}