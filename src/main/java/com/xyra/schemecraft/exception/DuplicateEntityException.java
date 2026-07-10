package com.xyra.schemecraft.exception;

/**
 * Thrown when an attempt is made to create a duplicate entity in the database.
 */
public class DuplicateEntityException extends DAOException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
