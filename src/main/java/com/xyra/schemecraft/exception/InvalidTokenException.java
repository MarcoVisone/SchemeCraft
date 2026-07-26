package com.xyra.schemecraft.exception;

public class InvalidTokenException extends ServiceException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}