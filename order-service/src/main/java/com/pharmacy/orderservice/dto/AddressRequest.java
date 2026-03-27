package com.pharmacy.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String label;

    @NotBlank
    private String line1;
    private String line2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String pincode;

    private Boolean isDefault;
}
