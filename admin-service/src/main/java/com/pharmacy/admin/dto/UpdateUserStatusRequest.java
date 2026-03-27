package com.pharmacy.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotBlank
    private String status; // ACTIVE | INACTIVE | SUSPENDED
}
