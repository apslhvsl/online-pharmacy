package com.pharmacy.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionReviewRequest {
    @NotNull
    private PrescriptionStatus status;
    private String remarks;
}
