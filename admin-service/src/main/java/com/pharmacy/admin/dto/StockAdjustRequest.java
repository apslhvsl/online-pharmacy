package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {
    private Long batchId;      // required: which batch to adjust
    private Integer adjustment; // positive = add, negative = deduct
    private String reason;
}
