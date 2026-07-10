package com.xyra.schemecraft.dao;

import java.math.BigDecimal;

public class ProductSearchCriteria {
    private String keywords;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private BigDecimal maxRating;
    private Boolean onlyWithDiscount;
    private String orderByColumn;
    private Boolean ascending = true;
    private String minecraftVersion;

    private int pageNumber = 1;
    private int pageSize = 20;

    public ProductSearchCriteria() {
    }

    public ProductSearchCriteria(String keywords) {
        this.keywords = keywords;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinRating() {
        return minRating;
    }

    public void setMinRating(BigDecimal minRating) {
        this.minRating = minRating;
    }

    public BigDecimal getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(BigDecimal maxRating) {
        this.maxRating = maxRating;
    }

    public Boolean getOnlyWithDiscount() {
        return onlyWithDiscount;
    }

    public void setOnlyWithDiscount(Boolean onlyWithDiscount) {
        this.onlyWithDiscount = onlyWithDiscount;
    }

    public String getOrderByColumn() {
        return orderByColumn;
    }

    public void setOrderByColumn(String orderByColumn) {
        this.orderByColumn = orderByColumn;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "ProductSearchCriteria{" +
                "keywords='" + keywords + '\'' +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", minRating=" + minRating +
                ", maxRating=" + maxRating +
                ", onlyWithDiscount=" + onlyWithDiscount +
                ", orderByColumn='" + orderByColumn + '\'' +
                ", ascending=" + ascending +
                ", minecraftVersion='" + minecraftVersion + '\'' +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                '}';
    }
}
