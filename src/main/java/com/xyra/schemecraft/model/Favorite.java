package com.xyra.schemecraft.model;

import java.io.Serializable;

public class Favorite implements Serializable {
    private static final long serialVersionUID = 1L;

    private String favoriteId;
    private String accountId;
    private String productId;

    public Favorite() {
    }

    public Favorite(String favoriteId, String accountId, String productId) {
        this.favoriteId = favoriteId;
        this.accountId = accountId;
        this.productId = productId;
    }

    public String getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
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
        return "Favorite{" +
                "favoriteId='" + favoriteId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
