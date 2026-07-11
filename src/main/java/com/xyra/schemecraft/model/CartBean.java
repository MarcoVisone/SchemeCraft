package com.xyra.schemecraft.model;

import java.io.Serializable;

public class CartBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String productId;

    public CartBean() {
    }

    public CartBean(String accountId, String productId) {
        this.accountId = accountId;
        this.productId = productId;
    }

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

    @Override
    public String toString() {
        return "Cart{" +
                "accountId=" + accountId +
                ", productId=" + productId +
                '}';
    }
}
