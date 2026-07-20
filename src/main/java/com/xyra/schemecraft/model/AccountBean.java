package com.xyra.schemecraft.model;

import com.xyra.schemecraft.constant.ValidationConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Represents the account domain model and data transfer object within the application.
 */
public class AccountBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the account. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Unique alphanumeric username. */
    @NotBlank(message = "Username cannot be blank")
    @Size(min = ValidationConstants.USERNAME_MIN_LENGTH, max = ValidationConstants.USERNAME_MAX_LENGTH, message = "Username must be between {min} and {max} characters")
    @Pattern(regexp = ValidationConstants.USERNAME_REGEXP, message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /** Unique email address associated with the account. */
    @NotBlank(message = "Email address cannot be blank")
    @Email(regexp = ValidationConstants.EMAIL_REGEXP,
            message = "Email address must be syntactically valid")
    private String email;

    /** Reference identifier for the user's country of residence. */
    private String countryId;

    /** Reference identifier for the preferred currency. */
    private String currencyId;

    /** Reference identifier for the user's language. */
    private String languageId;

    /** Server file path pointing to the user's banner image. */
    private String bannerPath;

    /** Personal description written by the user. */
    private String bio;

    /** Timestamp indicating when the account was registered. */
    private LocalDateTime createdAt;

    /** Status flag indicating if the account is active. */
    private boolean isActive;

    /** Privilege flag indicating if the user has administrative rights. */
    private boolean isAdmin;

    /** Secure cryptographic hash of the user's password. */
    @NotBlank(message = "Password hash cannot be blank")
    private String passwordHash;

    /** Server file path pointing to the user's profile image. */
    private String profileImagePath;

    /**
     * Default no-argument constructor.
     */
    public AccountBean() {}

    /**
     * Constructs a partially-initialized AccountBean without a registration timestamp.
     *
     * @param accountId        Unique identifier for the account
     * @param username         Unique username of the user
     * @param email            Primary email address
     * @param countryId        Reference to the user's country code
     * @param currencyId       Reference to the user's preferred currency code
     * @param languageId       Reference to the user's preferred language code
     * @param bannerPath       File path to the account's banner image
     * @param bio              User-provided short biography
     * @param isActive         Initial active state of the account
     * @param isAdmin          Initial admin privilege state
     * @param passwordHash     Secure cryptographic password hash
     * @param profileImagePath File path to the account's profile image
     */
    public AccountBean(String accountId, String username, String email, String countryId, String currencyId,
                       String languageId, String bannerPath, String bio, boolean isActive, boolean isAdmin,
                       String passwordHash, String profileImagePath) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.countryId = countryId;
        this.currencyId = currencyId;
        this.languageId = languageId;
        this.bannerPath = bannerPath;
        this.bio = bio;
        this.isActive = isActive;
        this.isAdmin = isAdmin;
        this.passwordHash = passwordHash;
        this.profileImagePath = profileImagePath;
    }

    /**
     * Constructs a fully-initialized AccountBean.
     *
     * @param accountId        Unique identifier for the account
     * @param username         Unique username of the user
     * @param email            Primary email address
     * @param countryId        Reference to the user's country code
     * @param currencyId       Reference to the user's preferred currency code
     * @param languageId       Reference to the user's preferred language code
     * @param bannerPath       File path to the account's banner image
     * @param bio              User-provided short biography
     * @param createdAt        The timestamp when the account was originally created
     * @param isActive         Active state of the account
     * @param isAdmin          Admin privilege state
     * @param passwordHash     Secure cryptographic password hash
     * @param profileImagePath File path to the account's profile image
     */
    public AccountBean(String accountId, String username, String email, String countryId, String currencyId,
                       String languageId, String bannerPath, String bio, LocalDateTime createdAt, boolean isActive,
                       boolean isAdmin, String passwordHash, String profileImagePath) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.countryId = countryId;
        this.currencyId = currencyId;
        this.languageId = languageId;
        this.bannerPath = bannerPath;
        this.bio = bio;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.isAdmin = isAdmin;
        this.passwordHash = passwordHash;
        this.profileImagePath = profileImagePath;
    }

    // --- Getters and Setters ---

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public void applyDefaultsIfMissing() {
        if (this.bannerPath == null) {
            this.bannerPath = "uploads/banners/default-banner.png";
        }
        if (this.profileImagePath == null) {
            this.profileImagePath = "uploads/avatars/default-avatar.png";
        }
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this account with another object for equality.
     * Equality is determined by primary identifiers and status flags.
     *
     * @param o The object to compare with
     * @return true if the objects are equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountBean that = (AccountBean) o;
        return isActive == that.isActive &&
                isAdmin == that.isAdmin &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(email, that.email);
    }

    /**
     * Generates a hash code value for this account object based on its primary fields.
     *
     * @return The integer hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountId, username, email, isActive, isAdmin);
    }

    /**
     * Returns a string representation of the AccountBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "AccountBean{" +
                "accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", countryId='" + countryId + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", languageId='" + languageId + '\'' +
                ", bannerPath='" + bannerPath + '\'' +
                ", bio='" + bio + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", isAdmin=" + isAdmin +
                ", passwordHash='[PROTECTED]'" +
                ", profileImagePath='" + profileImagePath + '\'' +
                '}';
    }
}
