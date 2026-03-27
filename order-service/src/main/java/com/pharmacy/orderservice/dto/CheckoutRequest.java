package com.pharmacy.orderservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    // Step 2: address
    private Long addressId;
    private AddressRequest inlineAddress; // alternative to addressId

    // Step 3: prescription
    private Long prescriptionId;
}
