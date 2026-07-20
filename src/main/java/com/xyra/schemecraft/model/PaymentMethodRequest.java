package com.xyra.schemecraft.model;

public record PaymentMethodRequest(
        int methodType,
        boolean isDefault,

        String cardNumber,
        String cardExpiration,
        String cvv,
        String cardBrand,

        String paypalEmail,

        String accountId
) {}