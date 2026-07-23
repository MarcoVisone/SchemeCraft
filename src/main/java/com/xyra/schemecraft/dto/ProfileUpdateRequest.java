package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) carrying the payload required to update account profile details.
 *
 * @param accountId        Unique identifier of the target account to update
 * @param countryId        Identifier for the user's country preference
 * @param currencyId       Identifier for the user's preferred currency
 * @param languageId       Identifier for the user's preferred language
 * @param bannerPath       Relative path or URL to the user's custom profile banner image
 * @param bio              Biographical summary or profile description
 * @param profileImagePath Relative path or URL to the user's avatar image
 */
public record ProfileUpdateRequest(
        String accountId,
        String countryId,
        String currencyId,
        String languageId,
        String bannerPath,
        String bio,
        String profileImagePath
) {
    /**
     * Compact constructor performing parameter sanitization, mandatory field validation,
     * and fallback initialization for image paths.
     *
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public ProfileUpdateRequest {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for profile updates");
        }

        accountId = accountId.trim();

        countryId = countryId != null ? countryId.trim() : null;

        currencyId = currencyId != null ? currencyId.trim() : null;

        languageId = languageId != null ? languageId.trim() : null;

        if (bannerPath == null || bannerPath.trim().isEmpty()) {
            bannerPath = "uploads/banners/default-banner.png";
        } else {
            bannerPath = bannerPath.trim();
        }

        bio = bio != null ? bio.trim() : null;

        if (profileImagePath == null || profileImagePath.trim().isEmpty()) {
            profileImagePath = "uploads/avatars/default-avatar.png";
        } else {
            profileImagePath = profileImagePath.trim();
        }
    }
}
