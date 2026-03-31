package com.pharmacy.catalog.exception;

// thrown when a user uploads a file type we don't accept (e.g. .exe, .docx)
public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String message) {
        super(message);
    }
}