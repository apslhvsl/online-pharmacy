package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationEvent {
    private Long userId;
    private String userEmail;
    private String userName;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime updatedAt;
}
