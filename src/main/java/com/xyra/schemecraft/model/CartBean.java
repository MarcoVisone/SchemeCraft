package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

/**
 * Represents the Cart domain model and data transfer object within the application.
 */
public class CartBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the associated Account owning the cart. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Unique identifier of the Product added to the cart. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /**
     * Default no-argument constructor.
     */
    public CartBean() {
    }

    /**
     * Constructs a fully-initialized CartBean.
     *
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product
     */
    public CartBean(String accountId, String productId) {
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
     * Compares this cart entry with another object for equality.
     * Two entries are considered equal if they reference the same account and product.
     *
     * @param o The reference object to compare
     * @return true if this object is equal to the argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartBean cartBean = (CartBean) o;
        return Objects.equals(accountId, cartBean.accountId) &&
                Objects.equals(productId, cartBean.productId);
    }

    /**
     * Generates a hash code based on the composite identifiers.
     *
     * @return A hash code value for this cart bean entry
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountId, productId);
    }

    /**
     * Returns a string representation of the CartBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "CartBean{" + // Fixed class name consistency
                "accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
