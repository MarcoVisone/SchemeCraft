package com.xyra.schemecraft.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Centralizes creation, reading, and deletion of HTTP cookies used across the app,
 * currently the remember-me login token.
 */
public final class CookieUtils {

    public static final String REMEMBER_ME_COOKIE_NAME = "rememberToken";
    private static final int REMEMBER_ME_MAX_AGE_SECONDS = 60 * 60 * 24 * 30; // 30 days

    public static final String CART_COOKIE_NAME = "cartItems";
    private static final int CART_MAX_AGE_SECONDS = 60 * 60 * 24 * 30; // 30 days
    private static final int CART_MAX_ITEMS = 50;

    // UUID v4-style validation, matches the productId format used across the DB (VARCHAR(36))
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

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

    /**
     * Persists the guest cart as a comma-separated list of product IDs in a cookie.
     * If the list is empty, the cookie is cleared instead of writing an empty value.
     */
    public static void setCartCookie(HttpServletResponse resp, List<String> productIds, String contextPath) {
        if (productIds == null || productIds.isEmpty()) {
            clearCartCookie(resp, contextPath);
            return;
        }

        String rawValue = String.join(",", productIds);
        String encodedValue = URLEncoder.encode(rawValue, StandardCharsets.UTF_8);

        Cookie cookie = new Cookie(CART_COOKIE_NAME, encodedValue);
        cookie.setHttpOnly(true);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setMaxAge(CART_MAX_AGE_SECONDS);
        resp.addCookie(cookie);
    }

    /**
     * Reads and sanitizes the guest cart cookie, returning a clean list of product IDs.
     * Defensive by design: never trusts the raw cookie content, since it is fully
     * client-controlled. Malformed tokens are silently discarded and the result is
     * capped to prevent an oversized or maliciously inflated cookie from being processed.
     *
     * @return a validated list of product IDs, empty if the cookie is missing or invalid
     */

    public static List<String> getCartProductIds(HttpServletRequest req) {
        List<String> result = new ArrayList<>();

        if (req.getCookies() == null) {
            return result;
        }

        String rawValue = null;
        for (Cookie cookie : req.getCookies()) {
            if (CART_COOKIE_NAME.equals(cookie.getName())) {
                rawValue = cookie.getValue();
                break;
            }
        }

        if (rawValue == null || rawValue.isBlank()) {
            return result;
        }

        String decodedValue;
        try {
            decodedValue = URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return result;
        }

        for (String token : decodedValue.split(",")) {
            String trimmed = token.trim();
            if (UUID_PATTERN.matcher(trimmed).matches() && !result.contains(trimmed)) {
                result.add(trimmed);
            }
            if (result.size() >= CART_MAX_ITEMS) {
                break;
            }
        }

        return result;
    }

    /**
     * Clears the guest cart cookie, e.g. after checkout or when the cart becomes empty.
     */
    public static void clearCartCookie(HttpServletResponse resp, String contextPath) {
        Cookie cookie = new Cookie(CART_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }
}