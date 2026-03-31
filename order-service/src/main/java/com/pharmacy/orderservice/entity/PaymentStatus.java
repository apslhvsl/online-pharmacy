package com.pharmacy.orderservice.entity;

public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUND_INITIATED,
    REFUNDED  // final state after refund is settled
}
