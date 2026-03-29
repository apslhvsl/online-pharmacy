package com.pharmacy.orderservice.entity;

public enum OrderStatus {

    // ── Checkout flow ─────────────────────────────────────────────────
    DRAFT,                  // cart converted to order, not yet submitted
    CHECKOUT_STARTED,       // user is actively in the checkout process
    PRESCRIPTION_PENDING,   // waiting for pharmacist to review uploaded prescription
    PRESCRIPTION_APPROVED,  // pharmacist approved the prescription
    PRESCRIPTION_REJECTED,  // pharmacist rejected the prescription
    PAYMENT_PENDING,        // prescription cleared (or not required), awaiting payment
    PAID,                   // payment confirmed

    // ── Fulfillment flow ──────────────────────────────────────────────
    PACKED,                 // warehouse has packed the order
    OUT_FOR_DELIVERY,       // handed to delivery agent
    DELIVERED,              // customer received the order

    // ── Exception flows ───────────────────────────────────────────────
    CUSTOMER_CANCELLED,     // cancelled by the customer
    ADMIN_CANCELLED,        // cancelled by an admin (separate for reporting)
    PAYMENT_FAILED,         // payment gateway returned a failure

    // ── Return & refund flow ──────────────────────────────────────────
    RETURN_REQUESTED,       // customer initiated a return
    REFUND_INITIATED,       // refund has been triggered (not yet settled)
    REFUND_COMPLETED        // refund fully settled to customer
}
