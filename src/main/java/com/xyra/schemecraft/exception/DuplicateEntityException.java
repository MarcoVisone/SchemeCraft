package com.xyra.schemecraft.exception;

/**
 * Thrown when an attempt is made to create a duplicate entity in the database.
 */
public class DuplicateEntityException extends DAOException {

    public enum ConflictingField {
        ACCOUNT,
        USERNAME,
        EMAIL,
        UNKNOWN
    }

    private final ConflictingField conflictingField;

    public DuplicateEntityException(String message) {
        super(message);
        this.conflictingField = ConflictingField.UNKNOWN;
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
        this.conflictingField = ConflictingField.UNKNOWN;
    }

    public DuplicateEntityException(String message, ConflictingField conflictingField) {
        super(message);
        this.conflictingField = conflictingField;
    }

    public ConflictingField getConflictingField() {
        return conflictingField;
    }
}
