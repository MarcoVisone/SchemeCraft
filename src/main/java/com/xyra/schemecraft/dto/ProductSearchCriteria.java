package com.xyra.schemecraft.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) encapsulating dynamic query search parameters, pagination,
 * and sorting criteria for the SchemeCraft product catalog.
 * Includes defensive validations to prevent invalid values and potential performance issues.
 */
public class ProductSearchCriteria {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MIN_PAGE_NUMBER = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final BigDecimal RATING_MAX_LIMIT = new BigDecimal("5.0");

    private String keywords;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private BigDecimal maxRating;
    private Boolean onlyWithDiscount;
    private String orderByColumn;
    private Boolean ascending = true;
    private String minecraftVersion;

    private int pageNumber = MIN_PAGE_NUMBER;
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * Default constructor initializing search criteria with default pagination settings.
     */
    public ProductSearchCriteria() {
    }

    /**
     * Overloaded constructor initializing search criteria with basic keywords.
     *
     * @param keywords Search keywords query
     */
    public ProductSearchCriteria(String keywords) {
        setKeywords(keywords);
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = (keywords != null) ? keywords.trim() : null;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinRating() {
        return minRating;
    }

    public void setMinRating(BigDecimal minRating) {
        if (minRating != null) {
            if (minRating.compareTo(BigDecimal.ZERO) < 0 || minRating.compareTo(RATING_MAX_LIMIT) > 0) {
                throw new IllegalArgumentException("Minimum rating must be between 0.0 and 5.0");
            }
        }
        this.minRating = minRating;
    }

    public BigDecimal getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(BigDecimal maxRating) {
        if (maxRating != null) {
            if (maxRating.compareTo(BigDecimal.ZERO) < 0 || maxRating.compareTo(RATING_MAX_LIMIT) > 0) {
                throw new IllegalArgumentException("Maximum rating must be between 0.0 and 5.0");
            }
        }
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
        this.orderByColumn = (orderByColumn != null) ? orderByColumn.trim() : null;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending == null || ascending;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = (minecraftVersion != null) ? minecraftVersion.trim() : null;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = Math.max(pageNumber, MIN_PAGE_NUMBER);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        } else this.pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
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
