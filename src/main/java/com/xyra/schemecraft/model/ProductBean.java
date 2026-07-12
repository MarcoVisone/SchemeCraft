package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String accountId;
    private String currencyId;
    private BigDecimal averageRating;
    private LocalDateTime createdAt;
    private BigDecimal discount;
    private String description;
    private boolean isActive;
    private LocalDateTime latestUpdate;
    private BigDecimal price;
    private String productName;
    private Integer stockQuantity;
    private Integer totalDownloads;
    private Integer totalReviews;

    public ProductBean() {
    }

    public ProductBean(String productId, String accountId, String currencyId, BigDecimal discount, String description,
                       boolean isActive, BigDecimal price, String productName, Integer stockQuantity) {
        this.productId = productId;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.discount = discount;
        this.description = description;
        this.isActive = isActive;
        this.price = price;
        this.productName = productName;
        this.stockQuantity = stockQuantity;
    }

    public ProductBean(String productId, String accountId, String currencyId, BigDecimal averageRating,
                       LocalDateTime createdAt, BigDecimal discount, String description, boolean isActive,
                       LocalDateTime latestUpdate, BigDecimal price, String productName, Integer stockQuantity,
                       Integer totalDownloads, Integer totalReviews) {
        this.productId = productId;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.averageRating = averageRating;
        this.createdAt = createdAt;
        this.discount = discount;
        this.description = description;
        this.isActive = isActive;
        this.latestUpdate = latestUpdate;
        this.price = price;
        this.productName = productName;
        this.stockQuantity = stockQuantity;
        this.totalDownloads = totalDownloads;
        this.totalReviews = totalReviews;
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

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getLatestUpdate() {
        return latestUpdate;
    }

    public void setLatestUpdate(LocalDateTime latestUpdate) {
        this.latestUpdate = latestUpdate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(Integer totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }


    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", averageRating=" + averageRating +
                ", createdAt=" + createdAt +
                ", discount=" + discount +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", latestUpdate=" + latestUpdate +
                ", price=" + price +
                ", productName='" + productName + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", totalDownloads=" + totalDownloads +
                ", totalReviews=" + totalReviews +
                '}';
    }
}
