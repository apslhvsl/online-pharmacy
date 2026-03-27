package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionReviewRequest {
    private String status; // APPROVED or REJECTED
    private String remarks;
}
