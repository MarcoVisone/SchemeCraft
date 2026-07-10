package com.xyra.schemecraft.model;

import java.io.Serializable;

public class ProductCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String productId;

    public ProductCategory() {
    }

    public ProductCategory(String categoryId, String productId) {
        this.categoryId = categoryId;
        this.productId = productId;
    }

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

    @Override
    public String toString() {
        return "ProductCategory{" +
                "categoryId='" + categoryId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
