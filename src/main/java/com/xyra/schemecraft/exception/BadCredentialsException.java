package com.xyra.schemecraft.exception;

/**
 * Thrown when a user provides invalid credentials during authentication.
 */
public class BadCredentialsException extends ServiceException {

    public BadCredentialsException(String message) {
        super(message);
    }
}
