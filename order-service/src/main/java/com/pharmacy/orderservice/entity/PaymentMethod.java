package com.pharmacy.orderservice.entity;

public enum PaymentMethod {
    COD,      // cash on delivery — marked paid immediately
    PREPAID,  // online payment gateway
    WALLET
}
