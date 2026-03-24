package com.pharmacy.catalog.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckResponse {
    private Long medicineId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private Boolean sufficient;
}