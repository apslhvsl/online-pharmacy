package com.pharmacy.orderservice.dto;

import lombok.*;

// snapshot of user info fetched from auth-service for notification events
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private Long id;
    private String name;
    private String email;
}
