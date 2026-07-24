package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

/**
 * Represents the ProductImage domain model and data transfer object within the application.
 */
public class ProductImageBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the image asset. */
    @NotBlank(message = "Image ID cannot be blank")
    private String imageId;

    /** Reference identifier of the associated Product. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /** File path directing to the image resource. */
    @NotBlank(message = "Image path cannot be blank")
    private String imagePath;

    private int displayOrder;

    /**
     * Default no-argument constructor.
     */
    public ProductImageBean() {
    }

    /**
     * Constructs a fully-initialized ProductImageBean.
     *
     * @param imageId   Unique image identifier
     * @param productId Associated product identifier
     * @param imagePath Path or URL pointing to the image file
     * @param displayOrder Order in which the image should be displayed
     */
    public ProductImageBean(String imageId, String productId, String imagePath, int displayOrder) {
        this.imageId = imageId;
        this.productId = productId;
        this.imagePath = imagePath;
        this.displayOrder = displayOrder;
    }

    // --- Getters and Setters ---

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this product image with another object for equality.
     * Equality is determined strictly by the unique imageId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same imageId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductImageBean that = (ProductImageBean) o;
        return Objects.equals(imageId, that.imageId);
    }

    /**
     * Generates a hash code based on the unique imageId.
     *
     * @return A hash code value for this product image bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(imageId);
    }

    /**
     * Returns a string representation of the ProductImageBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "ProductImageBean{" + // Fixed class name consistency
                "imageId='" + imageId + '\'' +
                ", productId='" + productId + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }
}
