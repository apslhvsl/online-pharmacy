package com.pharmacy.orderservice.dto;

import lombok.*;

// request body for the checkout address step
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    // Step 2: address
    private Long addressId;
    private AddressRequest inlineAddress; // alternative to addressId — creates a new address on the fly

    // Step 3: prescription
    private Long prescriptionId;
}
