package com.xyra.schemecraft.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) encapsulating dynamic query search parameters, pagination,
 * and sorting criteria for the SchemeCraft product catalog.
 * Includes defensive validations to prevent invalid values and potential performance issues.
 */
public class ProductSearchCriteria {

    /** Default number of records per page if not specified or invalid. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** Minimum allowed page index for pagination. */
    private static final int MIN_PAGE_NUMBER = 1;

    /** Maximum allowed page size boundary to prevent excessive memory consumption. */
    private static final int MAX_PAGE_SIZE = 100;

    /** Upper bound limit for product rating values. */
    private static final BigDecimal RATING_MAX_LIMIT = new BigDecimal("5.0");

    /** Search keywords filter query string. */
    private String keywords;

    /** Minimum price boundary filter. */
    private BigDecimal minPrice;

    /** Maximum price boundary filter. */
    private BigDecimal maxPrice;

    /** Minimum product rating score filter. */
    private BigDecimal minRating;

    /** Maximum product rating score filter. */
    private BigDecimal maxRating;

    /** Flag indicating whether to restrict results only to discounted products. */
    private Boolean onlyWithDiscount;

    /** Column or field name used for sorting query results. */
    private String orderByColumn;

    /** Flag indicating sorting direction; true for ascending, false for descending. */
    private Boolean ascending = true;

    /** Minecraft version compatibility filter. */
    private String minecraftVersion;

    /** Target category unique identifier filter. */
    private String categoryId;

    /** Current requested page number for paginated results (1-based index). */
    private int pageNumber = MIN_PAGE_NUMBER;

    /** Maximum quantity of records to retrieve per page. */
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

    // --- Getters and Setters ---

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
        } else {
            this.pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
        }
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = (categoryId != null) ? categoryId.trim() : null;
    }

    // --- Standard Object Override Methods ---

    /**
     * Returns a string representation of the ProductSearchCriteria object.
     *
     * @return Formatted string representation of this instance
     */
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
                ", categoryId='" + categoryId + '\'' +
                '}';
    }
}
