package com.xyra.schemecraft.exception;

public class PaymentTokenizationException extends ServiceException {

    private final String errorCode;

    public PaymentTokenizationException(String message) {
        super(message);
        this.errorCode = null;
    }

    public PaymentTokenizationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
