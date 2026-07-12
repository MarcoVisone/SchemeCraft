package com.xyra.schemecraft.service.gateway;

public record TokenizationResult(boolean success, String token, String brand,
                                 String lastFour, String expiration, String email, String errorCode) {

    public static TokenizationResult success(String token, String brand, String lastFour, String expiration) {
        return new TokenizationResult(true, token, brand, lastFour, expiration, null, null);
    }

    public static TokenizationResult successPayPal(String token, String paypalEmail) {
        return new TokenizationResult(true, token, paypalEmail, null, null, paypalEmail, null);
    }

    public static TokenizationResult failure(String errorCode) {
        return new TokenizationResult(false, null, null, null, null, null, errorCode);
    }
}
