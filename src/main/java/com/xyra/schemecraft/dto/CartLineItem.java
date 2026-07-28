package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.ProductBean;

/**
 * Read-only view of a single cart row: a product paired with its cover image path.
 * Used to render both the authenticated (DB-backed) and guest (cookie-backed) cart
 * with a single, uniform data shape.
 */
public class CartLineItem {

    private final ProductBean product;
    private final String coverImagePath; // null if the product has no uploaded images yet

    public CartLineItem(ProductBean product, String coverImagePath) {
        this.product = product;
        this.coverImagePath = coverImagePath;
    }

    public ProductBean getProduct() {
        return product;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }
}