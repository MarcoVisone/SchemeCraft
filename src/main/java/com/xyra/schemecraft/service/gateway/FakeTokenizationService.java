package com.xyra.schemecraft.service.gateway;

import java.util.UUID;

public class FakeTokenizationService {
    public TokenizationResult tokenizeCC(String cardNumber, String expiration, String cvv) {
        if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() < 13) {
            return TokenizationResult.failure("INVALID_CARD_NUMBER");
        }

        if(cvv == null || !cvv.matches("\\d{3,4}")) {
            return TokenizationResult.failure("INVALID_CVV");
        }

        String cleanedCC = cardNumber.replaceAll("\\s", "");
        String lastFour = cleanedCC.substring(cleanedCC.length() - 4);
        String brand = detectBrand(cleanedCC);
        String fakeToken = "tok_" + UUID.randomUUID();

        return TokenizationResult.success(fakeToken, brand, lastFour, expiration);
    }

    public TokenizationResult tokenizePP(String paypalEmail) {
        if (paypalEmail == null || !isValidEmail(paypalEmail)) {
            return TokenizationResult.failure("INVALID_PAYPAL_EMAIL");
        }

        String fakeToken = "tok_pp_" + UUID.randomUUID();

        return TokenizationResult.successPayPal(fakeToken, paypalEmail);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    private String detectBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "Visa";
        } else if (cardNumber.matches("5[1-5]\\d*")) {
            return "MasterCard";
        }
        return "Unknown";
    }
}
