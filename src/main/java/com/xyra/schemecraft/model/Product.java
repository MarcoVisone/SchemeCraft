package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String accountId;
    private String currencyId;
    private String productName;
    private BigDecimal discount;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean isActive;
    private LocalDateTime latestUpdate;
    private LocalDateTime createdAt;

    public Product() {
        this.discount = BigDecimal.ZERO;
        this.price = BigDecimal.ZERO;
        this.isActive = true;
    }

    public Product(String productId, String accountId, String currencyId, String productName, BigDecimal discount,
                   String description, BigDecimal price, Integer stockQuantity, boolean isActive,
                   LocalDateTime latestUpdate, LocalDateTime createdAt) {
        this.productId = productId;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.productName = productName;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.description = description;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.stockQuantity = stockQuantity;
        this.isActive = isActive;
        this.latestUpdate = latestUpdate;
        this.createdAt = createdAt;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public LocalDateTime getLatestUpdate() {
        return latestUpdate;
    }

    public void setLatestUpdate(LocalDateTime latestUpdate) {
        this.latestUpdate = latestUpdate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", productName='" + productName + '\'' +
                ", discount=" + discount +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", isActive=" + isActive +
                ", latestUpdate=" + latestUpdate +
                ", createdAt=" + createdAt +
                '}';
    }
}
