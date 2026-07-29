package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the persistent "Remember Me" authentication token entity within the application.
 * Stores selector IDs, hashed token secrets, ownership references, and creation timestamps.
 */
public class RememberTokenBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique public identifier  used to look up the token. */
    @NotBlank(message = "Token ID cannot be blank")
    @Size(max = 255, message = "Token ID cannot exceed {max} characters")
    private String tokenId;

    /** Unique identifier of the account associated with this token. */
    @NotBlank(message = "Account ID cannot be blank")
    @Size(max = 255, message = "Account ID cannot exceed {max} characters")
    private String accountId;

    /** Cryptographic hash of the raw persistent login token secret. */
    @NotBlank(message = "Token hash cannot be blank")
    private String tokenHash;

    /** Timestamp recording when the token was issued. */
    @NotNull(message = "Creation timestamp cannot be null")
    private LocalDateTime createdAt;

    /**
     * Default no-argument constructor.
     */
    public RememberTokenBean() {
    }

    /**
     * Constructs a fully-initialized RememberTokenBean.
     *
     * @param tokenId   Unique public token identifier
     * @param accountId Associated account identifier
     * @param tokenHash Hashed representation of the token secret
     * @param createdAt Creation timestamp of the token
     */
    public RememberTokenBean(String tokenId, String accountId, String tokenHash, LocalDateTime createdAt) {
        this.tokenId = tokenId;
        this.accountId = accountId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

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

    // --- Standard Object Override Methods ---

    /**
     * Compares this remember token with another object for equality.
     * Equality is determined strictly by the unique tokenId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same tokenId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RememberTokenBean that = (RememberTokenBean) o;
        return Objects.equals(tokenId, that.tokenId);
    }

    /**
     * Generates a hash code based on the unique tokenId.
     *
     * @return A hash code value for this remember token bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(tokenId);
    }

    /**
     * Returns a string representation of the RememberTokenBean object.
     * Note: The tokenHash field is intentionally omitted for security reasons.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "RememberTokenBean{" +
                "tokenId='" + tokenId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
