package com.xyra.schemecraft.model;

import com.xyra.schemecraft.constant.ValidationConstants;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Represents the Account Registration Request data transfer object within the application.
 */
public record AccountRegistrationRequest(

        @NotBlank(message = "Username cannot be blank")
        @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, max = ValidationConstants.USERNAME_MAX_LENGTH, message = "Username must be between {min} and {max} characters")
        @Pattern(regexp = ValidationConstants.USERNAME_REGEXP, message = "Username can only contain letters, numbers, and underscores")
        String username,

        @NotBlank(message = "Email address cannot be blank")
        @Email(regexp = ValidationConstants.EMAIL_REGEXP,
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
) {}