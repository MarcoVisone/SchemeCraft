package com.xyra.schemecraft.model;

/**
 * Data Transfer Object (DTO) representing the sanitized response payload
 * returned following a successful account registration.
 *
 * @param accountId        Unique generated identifier assigned to the new account
 * @param username         Registered username
 * @param email            Primary email address linked to the account
 * @param countryId        Identifier of the assigned primary country
 * @param languageId       Identifier of the selected interface language
 * @param currencyId       Identifier of the preferred currency
 * @param bio              Biographical summary or profile description
 * @param bannerPath       Relative path or URL to the user's profile banner image
 * @param profileImagePath Relative path or URL to the user's avatar image
 * @param isAdmin          Flag indicating administrative privileges status
 * @param isActive         Flag indicating account activation status
 */
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
) {
    /**
     * Compact constructor performing parameter sanitization and mandatory field assertion.
     *
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public AccountRegistrationResponse {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty in registration response");
        }

        accountId = accountId.trim();
        username = username != null ? username.trim() : null;
        email = email != null ? email.trim() : null;
        countryId = countryId != null ? countryId.trim() : null;
        languageId = languageId != null ? languageId.trim() : null;
        currencyId = currencyId != null ? currencyId.trim() : null;
        bio = bio != null ? bio.trim() : null;
        bannerPath = bannerPath != null ? bannerPath.trim() : null;
        profileImagePath = profileImagePath != null ? profileImagePath.trim() : null;
    }
}
