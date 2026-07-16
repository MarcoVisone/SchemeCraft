package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

/**
 * Represents an individual item within a shopping cart.
 */
public class CartItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The product associated with this cart item. */
    @NotNull(message = "Product details cannot be null")
    private ProductBean product;

    /**
     * Default no-argument constructor.
     */
    public CartItem() {
    }

    /**
     * Constructs a CartItem with the specified product.
     *
     * @param product The target product bean to be wrapped
     */
    public CartItem(ProductBean product) {
        this.product = product;
    }

    // --- Getters and Setters ---

    public ProductBean getProduct() {
        return product;
    }

    public void setProduct(ProductBean product) {
        this.product = product;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this CartItem with another object for equality.
     * Two CartItems are considered equal if they wrap the same product.
     *
     * @param o The reference object to compare
     * @return true if this object wraps an identical product; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(product, cartItem.product);
    }

    /**
     * Generates a hash code based on the wrapped product.
     *
     * @return A hash code value for this cart item
     */
    @Override
    public int hashCode() {
        return Objects.hash(product);
    }

    /**
     * Returns a string representation of the CartItem.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "CartItem{" +
                "product=" + product +
                '}';
    }
}
