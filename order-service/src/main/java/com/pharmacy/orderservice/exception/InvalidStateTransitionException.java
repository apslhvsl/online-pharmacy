package com.pharmacy.orderservice.exception;

// thrown when a requested order status transition isn't allowed by the state machine
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
