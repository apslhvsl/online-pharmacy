package com.pharmacy.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @NotNull @Min(0)
    private Integer quantity; // 0 = remove item
}
