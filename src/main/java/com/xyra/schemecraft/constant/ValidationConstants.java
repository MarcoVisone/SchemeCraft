package com.xyra.schemecraft.constant;

public final class ValidationConstants {

    private ValidationConstants() {}

    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    public static final String USERNAME_REGEXP = "^[a-zA-Z0-9_]+$";

    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
}