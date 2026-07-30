package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.ProductBean;

/**
 * Data Transfer Object (DTO) representing a single line item within a shopping cart,
 * pairing product domain details with its resolved visual representation.
 *
 * @param product        The product model entity associated with this line item
 * @param coverImagePath Relative path or URL to the product's primary cover image
 */
public record CartLineItem(

        ProductBean product,

        String coverImagePath
) {
    /**
     * Compact constructor performing parameter assertion, sanitization, and default fallback assignments.
     *
     * @throws IllegalArgumentException if the product is null
     */
    public CartLineItem {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null in cart line item");
        }

        if (coverImagePath == null || coverImagePath.trim().isEmpty()) {
            coverImagePath = "uploads/products/default-cover.png";
        } else {
            coverImagePath = coverImagePath.trim();
        }
    }

    public ProductBean getProduct() {
        return product();
    }

    public String getCoverImagePath() {
        return coverImagePath();
    }
}