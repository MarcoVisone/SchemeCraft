package com.xyra.schemecraft.model;

import java.io.Serializable;

public class ProductImageBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String imageId;
    private String productId;
    private String imagePath;

    public ProductImageBean() {
    }

    public ProductImageBean(String imageId, String productId, String imagePath) {
        this.imageId = imageId;
        this.productId = productId;
        this.imagePath = imagePath;
    }

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

    @Override
    public String toString() {
        return "ProductImage{" +
                "imageId='" + imageId + '\'' +
                ", productId='" + productId + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
