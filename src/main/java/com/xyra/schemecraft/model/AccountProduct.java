package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AccountProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String productId;
    private LocalDateTime unlockedAt;

    public AccountProduct() {
    }

    public AccountProduct(String accountId, String productId) {
        this.accountId = accountId;
        this.productId = productId;
    }

    public AccountProduct(String accountId, String productId, LocalDateTime unlockedAt) {
        this.accountId = accountId;
        this.productId = productId;
        this.unlockedAt = unlockedAt;
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

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    @Override
    public String toString() {
        return "AccountProduct{" +
                "accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                ", unlockedAt=" + unlockedAt +
                '}';
    }
}
