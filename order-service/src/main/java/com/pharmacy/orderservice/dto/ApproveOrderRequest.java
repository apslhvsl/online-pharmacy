package com.pharmacy.orderservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ApproveOrderRequest {
    /** Map of orderItemId → batchId (admin can override FEFO suggestion) */
    private Map<Long, Long> batchOverrides;
    private String note;
}
