package com.pharmacy.catalog.dto;

import com.pharmacy.catalog.entity.PrescriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionReviewRequest {

    @NotNull
    private PrescriptionStatus status; // APPROVED or REJECTED

    private String remarks;
}
