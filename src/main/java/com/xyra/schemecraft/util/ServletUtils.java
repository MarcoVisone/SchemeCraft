package com.xyra.schemecraft.util;

import javax.servlet.http.HttpServletRequest;

public final class ServletUtils {
    private ServletUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getActionPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return (pathInfo == null) ? "" : pathInfo;
    }
}
