package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReviewBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String productId;
    private String comment;
    private LocalDateTime createdAt;
    private boolean isVerifiedPurchase;
    private int rating;

    public ReviewBean() {
    }

    public ReviewBean(String accountId, String productId, String comment, boolean isVerifiedPurchase, int rating) {
        this.accountId = accountId;
        this.productId = productId;
        this.comment = comment;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.rating = rating;
    }

    public ReviewBean(String accountId, String productId, String comment, LocalDateTime createdAt,
                      boolean isVerifiedPurchase, int rating) {
        this.accountId = accountId;
        this.productId = productId;
        this.comment = comment;
        this.createdAt = createdAt;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.rating = rating;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Review{" +
                "accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", isVerifiedPurchase=" + isVerifiedPurchase +
                ", rating=" + rating +
                '}';
    }
}
