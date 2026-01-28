package com.alten.producttrial.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Unauthorized access to %s with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
