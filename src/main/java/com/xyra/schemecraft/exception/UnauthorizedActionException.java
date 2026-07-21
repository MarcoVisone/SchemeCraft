package com.xyra.schemecraft.exception;

public class UnauthorizedActionException extends ServiceException {

    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(String message, Throwable cause) {
        super(message, cause);
    }
}