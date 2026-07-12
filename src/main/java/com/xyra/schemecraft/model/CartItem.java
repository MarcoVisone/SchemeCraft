package com.xyra.schemecraft.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private ProductBean product;

    public CartItem(ProductBean product) {
        this.product = product;
    }

    public ProductBean getProduct() {
        return product;
    }

    public void setProduct(ProductBean product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "CartItem{" + "product=" + product + '}';
    }
}
