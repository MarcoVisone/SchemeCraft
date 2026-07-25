package com.xyra.schemecraft.util;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
