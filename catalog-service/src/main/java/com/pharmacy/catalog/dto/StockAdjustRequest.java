package com.pharmacy.catalog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @NotNull
    private Integer adjustment; // positive = add stock, negative = deduct

    @NotBlank
    private String reason;
}
