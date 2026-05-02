package com.pharmacy.admin.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ApproveOrderRequest {
    /** Map of orderItemId → batchId override */
    private Map<Long, Long> batchOverrides;
    private String note;
}
