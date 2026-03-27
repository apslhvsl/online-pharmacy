package com.pharmacy.orderservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {
    private Long medicineId;
    private Long batchId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private Boolean available;
}
