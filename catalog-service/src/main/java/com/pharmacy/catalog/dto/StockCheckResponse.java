package com.pharmacy.catalog.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckResponse {
    private Long medicineId;
    private Long batchId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private Boolean available;
    private LocalDate nextRestockDate;
}
