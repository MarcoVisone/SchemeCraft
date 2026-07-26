package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.constant.ValidationConstants;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
 * @param bannerPath        Relative path or URL to the user's custom profile banner image
 * @param profileImagePath   Relative path or URL to the user's avatar image
 */
public record AccountRegistrationRequest(

        @NotBlank(message = "Username cannot be blank")
        @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, max = ValidationConstants.USERNAME_MAX_LENGTH,
                message = "Username must be between {min} and {max} characters")
        @Pattern(regexp = ValidationConstants.USERNAME_REGEX,
                message = "Username can only contain letters, numbers, and underscores")
        String username,

        @NotBlank(message = "Email address cannot be blank")
        @Email(regexp = ValidationConstants.EMAIL_REGEX,
                message = "Email address must be syntactically valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String plainTextPassword,

        @NotBlank(message = "Country ID cannot be blank")
        String countryId,

        @NotBlank(message = "Currency ID cannot be blank")
        String currencyId,

        @NotBlank(message = "Language ID cannot be blank")
        String languageId,

        String bio,

        String bannerPath,

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
