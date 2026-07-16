package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

/**
 * Represents the AccountProduct domain model and data transfer object within the application.
 */
public class AccountProductBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the associated Account. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Unique identifier of the associated Product. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /** Timestamp indicating exactly when this product was unlocked by the user. */
    private LocalDateTime unlockedAt;

    /**
     * Default no-argument constructor.
     */
    public AccountProductBean() {
    }

    /**
     * Constructs a partial AccountProductBean without an explicit unlock timestamp.
     *
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product
     */
    public AccountProductBean(String accountId, String productId) {
        this.accountId = accountId;
        this.productId = productId;
    }

    /**
     * Constructs a fully-initialized AccountProductBean containing all association data.
     *
     * @param accountId  Unique identifier of the account
     * @param productId  Unique identifier of the product
     * @param unlockedAt Timestamp when the product was unlocked
     */
    public AccountProductBean(String accountId, String productId, LocalDateTime unlockedAt) {
        this.accountId = accountId;
        this.productId = productId;
        this.unlockedAt = unlockedAt;
    }

    // --- Getters and Setters ---

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this association record with another object for equality.
     * Equality is determined by the composite primary keys: accountId and productId.
     *
     * @param o The reference object with which to compare
     * @return true if this object is identical to the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountProductBean that = (AccountProductBean) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(productId, that.productId);
    }

    /**
     * Returns a hash code value for the association key.
     *
     * @return A hash code value based on accountId and productId
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountId, productId);
    }

    /**
     * Returns a string representation of the AccountProductBean.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "AccountProductBean{" +
                "accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                ", unlockedAt=" + unlockedAt +
                '}';
    }
}
