package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

/**
 * Represents the ProductCategory domain model and data transfer object within the application.
 */
public class ProductCategoryBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the associated Category. */
    @NotBlank(message = "Category ID cannot be blank")
    private String categoryId;

    /** Unique identifier of the associated Product. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /**
     * Default no-argument constructor.
     */
    public ProductCategoryBean() {
    }

    /**
     * Constructs a fully-initialized ProductCategoryBean coupling.
     *
     * @param categoryId Associated category identifier
     * @param productId  Associated product identifier
     */
    public ProductCategoryBean(String categoryId, String productId) {
        this.categoryId = categoryId;
        this.productId = productId;
    }

    // --- Getters and Setters ---

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this association record with another object for equality.
     * Equality is determined by the composite relation: both categoryId and productId must match.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same categoryId and productId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategoryBean that = (ProductCategoryBean) o;
        return Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(productId, that.productId);
    }

    /**
     * Generates a hash code based on the composite relational identifiers.
     *
     * @return A hash code value for this association bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(categoryId, productId);
    }

    /**
     * Returns a string representation of the ProductCategoryBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "ProductCategoryBean{" + // Fixed class name consistency
                "categoryId='" + categoryId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
