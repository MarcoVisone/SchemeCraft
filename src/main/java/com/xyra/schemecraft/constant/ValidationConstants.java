package com.xyra.schemecraft.constant;

/**
 * Utility class containing field validation constraints, length boundaries, and regular expression patterns.
 */
public final class ValidationConstants {

    /**
     * Standard RFC 5322 compliant regular expression pattern for validating email addresses.
     */
    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * Regular expression pattern enforcing alphanumeric usernames with underscores allowed.
     */
    public static final String USERNAME_REGEXP = "^[a-zA-Z0-9_]+$";

    /**
     * Minimum allowed length for a username string.
     */
    public static final int USERNAME_MIN_LENGTH = 3;

    /**
     * Maximum allowed length for a username string.
     */
    public static final int USERNAME_MAX_LENGTH = 50;

    /**
     * Private constructor to prevent instantiation of utility class.
     *
     * @throws AssertionError if instantiation is attempted
     */
    private ValidationConstants() {
        throw new AssertionError("ValidationConstants cannot be instantiated");
    }
}
