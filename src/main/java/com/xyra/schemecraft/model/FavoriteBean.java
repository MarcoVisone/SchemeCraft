package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

/**
 * Represents the Favorite domain model and data transfer object within the application.
 */
public class FavoriteBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the associated Account who marked the product as favorite. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Unique identifier of the Product marked as favorite. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /**
     * Default no-argument constructor.
     */
    public FavoriteBean() {
    }

    /**
     * Constructs a fully-initialized FavoriteBean.
     *
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product
     */
    public FavoriteBean(String accountId, String productId) {
        this.accountId = accountId;
        this.productId = productId;
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

    // --- Standard Object Override Methods ---

    /**
     * Compares this favorite entry with another object for equality.
     * Two entries are considered equal if they reference the same account and product.
     *
     * @param o The reference object to compare
     * @return true if this object is equal to the argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteBean that = (FavoriteBean) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(productId, that.productId);
    }

    /**
     * Generates a hash code based on the composite identifiers.
     *
     * @return A hash code value for this favorite bean entry
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountId, productId);
    }

    /**
     * Returns a string representation of the FavoriteBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "FavoriteBean{" + // Fixed class name consistency
                "accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
