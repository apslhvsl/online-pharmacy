package com.pharmacy.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @NotNull
    private Integer adjustment; // positive = add, negative = deduct

    @NotBlank
    private String reason;
}
