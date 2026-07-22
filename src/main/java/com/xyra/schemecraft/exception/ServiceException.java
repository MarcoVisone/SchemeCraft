package com.xyra.schemecraft.exception;

/**
 * Generic unchecked exception for service layer errors.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
