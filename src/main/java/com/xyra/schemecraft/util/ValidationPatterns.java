package com.xyra.schemecraft.util;

import java.util.regex.Pattern;

public final class ValidationPatterns {

    private ValidationPatterns() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,50}$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$";

    public static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
}
