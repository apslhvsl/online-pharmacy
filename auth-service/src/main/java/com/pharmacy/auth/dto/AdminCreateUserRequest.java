package com.pharmacy.auth.dto;

import com.pharmacy.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String mobile;
    @NotBlank
    private String password;
    @NotNull
    private Role role;
}
