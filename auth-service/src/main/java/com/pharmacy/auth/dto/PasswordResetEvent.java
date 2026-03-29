package com.pharmacy.auth.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetEvent implements Serializable {
    private Long userId;
    private String userEmail;
    private String userName;
    private String resetToken;
    private LocalDateTime expiresAt;
}
