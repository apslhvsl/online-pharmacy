package com.pharmacy.orderservice.exception;

// thrown when a cart item can't be fulfilled due to low or zero stock
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}