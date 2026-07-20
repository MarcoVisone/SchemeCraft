package com.xyra.schemecraft.model;

public record AccountRegistrationResponse(
        String accountId,
        String username,
        String email,
        String countryId,
        String languageId,
        String currencyId,
        String bio,
        String bannerPath,
        String profileImagePath,
        boolean isAdmin,
        boolean isActive
) {}
