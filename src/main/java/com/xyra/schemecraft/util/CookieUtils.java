package com.xyra.schemecraft.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Centralizes creation, reading, and deletion of HTTP cookies used across the app,
 * currently the remember-me login token.
 */
public final class CookieUtils {

    public static final String REMEMBER_ME_COOKIE_NAME = "rememberToken";
    private static final int REMEMBER_ME_MAX_AGE_SECONDS = 60 * 60 * 24 * 30; // 30 days

    private CookieUtils() {
    }

    public static void setRememberMeCookie(HttpServletResponse resp, String rawToken, String contextPath) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, rawToken);
        cookie.setHttpOnly(true);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setMaxAge(REMEMBER_ME_MAX_AGE_SECONDS);
        resp.addCookie(cookie);
    }

    public static String getRememberMeCookieValue(HttpServletRequest req) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : req.getCookies()) {
            if (REMEMBER_ME_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void clearRememberMeCookie(HttpServletResponse resp, String contextPath) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }
}