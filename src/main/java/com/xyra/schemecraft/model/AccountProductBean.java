package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AccountProductBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String productId;
    private LocalDateTime unlockedAt;

    public AccountProductBean() {
    }

    public AccountProductBean(String accountId, String productId) {
        this.accountId = accountId;
        this.productId = productId;
    }

    public AccountProductBean(String accountId, String productId, LocalDateTime unlockedAt) {
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
