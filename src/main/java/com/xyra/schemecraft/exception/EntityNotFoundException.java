package com.xyra.schemecraft.exception;

/**
 * Thrown when a requested database entity or resource cannot be found.
 */
public class EntityNotFoundException extends DAOException{
    public EntityNotFoundException(String message) {
        super(message);
    }
}
