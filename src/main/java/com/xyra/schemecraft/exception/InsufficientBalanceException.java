package com.xyra.schemecraft.exception;

/**
 * Thrown when an attempt is made to perform a transaction with an insufficient balance.
 */
public class InsufficientBalanceException extends ServiceException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
