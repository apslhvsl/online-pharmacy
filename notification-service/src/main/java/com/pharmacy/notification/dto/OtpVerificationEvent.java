package com.pharmacy.notification.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationEvent implements Serializable {
    private Long userId;
    private String userEmail;
    private String userName;
    private String otp;
    private LocalDateTime expiresAt;
}
