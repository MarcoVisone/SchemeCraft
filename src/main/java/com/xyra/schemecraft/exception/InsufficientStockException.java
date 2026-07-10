package com.xyra.schemecraft.exception;

/**
 * Thrown when an attempt is made to purchase more items than are available in stock.
 */
public class InsufficientStockException extends ServiceException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
