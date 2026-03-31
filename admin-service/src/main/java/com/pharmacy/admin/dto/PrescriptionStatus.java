package com.pharmacy.admin.dto;

public enum PrescriptionStatus {
    PENDING,
    APPROVED,
    REJECTED  // rejected prescriptions can't be used at checkout
}
