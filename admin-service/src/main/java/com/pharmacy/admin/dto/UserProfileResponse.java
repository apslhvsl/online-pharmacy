package com.pharmacy.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
