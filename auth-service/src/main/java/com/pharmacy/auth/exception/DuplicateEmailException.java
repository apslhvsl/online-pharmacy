package com.pharmacy.auth.exception;

// thrown when someone tries to register with an email that's already in use
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}