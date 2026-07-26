package com.xyra.schemecraft.constant;

import java.util.regex.Pattern;

/**
 * Utility class containing field validation constraints, length boundaries,
 * regular expression strings, and pre-compiled Pattern instances for high-performance matching.
 */
public final class ValidationConstants {

    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{" + USERNAME_MIN_LENGTH + "," + USERNAME_MAX_LENGTH + "}$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{" + PASSWORD_MIN_LENGTH + "," + PASSWORD_MAX_LENGTH + "}$";

    public static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws AssertionError if instantiation is attempted
     */
    private ValidationConstants() {
        throw new AssertionError("ValidationConstants cannot be instantiated");
    }
}
