package com.xyra.schemecraft.exception;

public class PaymentDeclinedException extends ServiceException {
    public PaymentDeclinedException(String message) {
        super(message);
    }

    public PaymentDeclinedException(String message, Throwable cause) {
        super(message, cause);
    }
}
