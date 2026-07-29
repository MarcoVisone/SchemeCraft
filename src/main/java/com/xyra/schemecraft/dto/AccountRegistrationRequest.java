package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) carrying the payload required to process a new user account registration.
 *
 * @param username          Unique display name chosen by the user
 * @param email             Primary email address associated with the account
 * @param plainTextPassword Raw password entered by the user
 * @param countryId         Identifier of the user's primary country
 * @param currencyId        Identifier of the user's preferred currency
 * @param languageId        Identifier of the user's preferred interface language
 * @param bio               Optional biographical summary or profile description
 * @param profileImagePath   Relative path or URL to the user's avatar image
 */
public record AccountRegistrationRequest(

        String username,

        String email,

        String plainTextPassword,

        String countryId,

        String currencyId,

        String languageId,

        String bio,

        String profileImagePath
) {
        /**
         * Compact constructor performing parameter sanitization and default fallback assignments.
         */
        public AccountRegistrationRequest {
                username = username != null ? username.trim() : null;
                email = email != null ? email.trim() : null;
                plainTextPassword = plainTextPassword != null ? plainTextPassword.trim() : null;
                countryId = countryId != null ? countryId.trim() : null;
                currencyId = currencyId != null ? currencyId.trim() : null;
                languageId = languageId != null ? languageId.trim() : null;
                bio = bio != null ? bio.trim() : null;

                if (profileImagePath == null || profileImagePath.trim().isEmpty()) {
                        profileImagePath = "uploads/avatars/default-avatar.png";
                } else {
                        profileImagePath = profileImagePath.trim();
                }
        }
}
