package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) carrying the payload required to add or register a new payment method for an account.
 *
 * @param methodType     Numeric identifier or code representing the type of payment method
 * @param isDefault      Flag indicating whether this payment method should be set as the primary default
 * @param cardNumber     Primary account/card number
 * @param cardExpiration Expiration date string associated with the card
 * @param cvv            Card Verification Value security code
 * @param cardBrand      Issuing brand or network of the card
 * @param paypalEmail    Associated email address for PayPal payment methods
 * @param accountId      Unique identifier of the account owner
 */
public record PaymentMethodRequest(

        int methodType,

        boolean isDefault,

        String cardNumber,

        String cardExpiration,

        String cvv,

        String cardBrand,

        String paypalEmail,

        String accountId
) {
    /**
     * Compact constructor performing parameter sanitization and basic input validation.
     *
     * @throws IllegalArgumentException if mandatory fields like Account ID are missing
     */
    public PaymentMethodRequest {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for payment method creation");
        }

        accountId = accountId.trim();
        cardNumber = cardNumber != null ? cardNumber.trim() : null;
        cardExpiration = cardExpiration != null ? cardExpiration.trim() : null;
        cvv = cvv != null ? cvv.trim() : null;
        cardBrand = cardBrand != null ? cardBrand.trim() : null;
        paypalEmail = paypalEmail != null ? paypalEmail.trim() : null;
    }
}
