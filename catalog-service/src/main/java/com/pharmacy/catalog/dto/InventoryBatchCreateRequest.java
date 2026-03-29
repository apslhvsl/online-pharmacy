package com.pharmacy.catalog.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchCreateRequest {

    @NotNull
    private Long medicineId;

    @NotBlank
    private String batchNumber;

    @NotNull @Future
    private LocalDate expiryDate;

    @NotNull @Positive
    private BigDecimal price;

    @NotNull @Positive
    private Integer quantity;
}
