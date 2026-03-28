package com.pharmacy.notification.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationEvent implements Serializable {
    private Long userId;
    private String userEmail;
    private String userName;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime updatedAt;
}
