package com.xyra.schemecraft.exception;

public class InactiveEntityException extends ServiceException {
    public InactiveEntityException(String message) {
        super(message);
    }
    public InactiveEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
