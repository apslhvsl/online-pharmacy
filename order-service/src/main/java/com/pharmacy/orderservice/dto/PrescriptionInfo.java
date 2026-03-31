package com.pharmacy.orderservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// minimal prescription info returned by catalog-service for checkout validation
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionInfo {
    private Long id;
    private Long userId;
    private String status;       // PENDING | APPROVED | REJECTED | EXPIRED
    private LocalDateTime validTill;
}
