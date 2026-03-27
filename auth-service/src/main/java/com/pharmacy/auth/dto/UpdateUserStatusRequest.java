package com.pharmacy.auth.dto;

import com.pharmacy.auth.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull
    private UserStatus status;
}
