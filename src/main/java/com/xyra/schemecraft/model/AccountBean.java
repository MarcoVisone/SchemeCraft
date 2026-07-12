package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AccountBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String username;
    private String email;
    private String countryId;
    private String currencyId;
    private String languageId;
    private String bannerPath;
    private String bio;
    private LocalDateTime createdAt;
    private boolean isActive;
    private boolean isAdmin;
    private String passwordHash;
    private String profileImagePath;

    public AccountBean() {
    }

    public AccountBean(String accountId, String username, String email, String countryId, String currencyId,
                       String languageId, String bannerPath, String bio, boolean isActive, boolean isAdmin,
                       String passwordHash, String profileImagePath) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.countryId = countryId;
        this.currencyId = currencyId;
        this.bannerPath = bannerPath;
        this.bio = bio;
        this.languageId = languageId;
        this.isActive = isActive;
        this.isAdmin = isAdmin;
        this.passwordHash = passwordHash;
        this.profileImagePath = profileImagePath;
    }

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
        isActive = active;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
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

    @Override
    public String toString() {
        return "Account{" +
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
                ", passwordHash='" + passwordHash + '\'' +
                ", profileImagePath='" + profileImagePath + '\'' +
                '}';
    }
}
