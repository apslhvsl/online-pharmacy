package com.pharmacy.catalog.entity;

public enum PrescriptionStatus {
    PENDING,    // waiting for pharmacist review
    APPROVED,
    REJECTED,
    EXPIRED     // valid_till date has passed
}