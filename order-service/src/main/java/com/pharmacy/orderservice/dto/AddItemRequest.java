package com.pharmacy.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {

    @NotNull
    private Long batchId;

    @NotNull @Min(1)
    private Integer quantity;
}
