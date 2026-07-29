package com.xyra.schemecraft.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a persistent "remember me" login token, allowing a user
 * to stay logged in across browser restarts without re-entering credentials.
 * Multiple tokens can exist per account (e.g. one per device/browser).
 */
public class RememberTokenBean {

    private String tokenId;
    private String accountId;
    private String tokenHash;
    private LocalDateTime createdAt;

    public RememberTokenBean() {
    }

    public RememberTokenBean(String tokenId, String accountId, String tokenHash, LocalDateTime createdAt) {
        this.tokenId = tokenId;
        this.accountId = accountId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RememberTokenBean that = (RememberTokenBean) o;
        return Objects.equals(tokenId, that.tokenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId);
    }

    @Override
    public String toString() {
        return "RememberTokenBean{" +
                "tokenId='" + tokenId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
