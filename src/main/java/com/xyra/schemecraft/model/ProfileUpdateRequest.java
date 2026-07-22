package com.xyra.schemecraft.model;

/**
 * Data Transfer Object (DTO) carrying the payload required to update account profile details.
 *
 * @param accountId        Unique identifier of the target account to update
 * @param bio              Biographical summary or profile description
 * @param bannerPath       Relative path or URL to the user's custom profile banner image
 * @param profileImagePath Relative path or URL to the user's avatar image
 */
public record ProfileUpdateRequest(
        String accountId,
        String bio,
        String bannerPath,
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
        bio = bio != null ? bio.trim() : null;

        if (bannerPath == null || bannerPath.trim().isEmpty()) {
            bannerPath = "uploads/banners/default-banner.png";
        } else {
            bannerPath = bannerPath.trim();
        }

        if (profileImagePath == null || profileImagePath.trim().isEmpty()) {
            profileImagePath = "uploads/avatars/default-avatar.png";
        } else {
            profileImagePath = profileImagePath.trim();
        }
    }
}
