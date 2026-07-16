package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the Product domain model and data transfer object within the application.
 */
public class ProductBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the product. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /** Reference identifier of the Account that owns this product. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Reference identifier of the Currency used for pricing this product. */
    @NotBlank(message = "Currency ID cannot be blank")
    private String currencyId;

    /** Average customer rating score. */
    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Average rating cannot exceed {value}")
    private BigDecimal averageRating;

    /** Timestamp indicating when the product was published. */
    private LocalDateTime createdAt;

    /** Flat or percentage-based discount value applied to the product. */
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discount;

    /** Detailed textual description of the product. */
    private String description;

    /** Flag indicating if the product is currently visible and purchasable in the catalog. */
    private boolean isActive;

    /** Timestamp of the latest product update. */
    private LocalDateTime latestUpdate;

    /** Base price of the product before discounts and taxes. */
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal price;

    /** Display name of the product. */
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 150, message = "Product name must be between {min} and {max} characters")
    private String productName;

    /** Available inventory quantity. Can be null if the product is a digital unlimited download. */
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    /** Total number of digital downloads completed for this product. */
    @Min(value = 0, message = "Total downloads cannot be negative")
    private Integer totalDownloads;

    /** Total number of written reviews associated with this product. */
    @Min(value = 0, message = "Total reviews cannot be negative")
    private Integer totalReviews;

    /**
     * Default no-argument constructor.
     */
    public ProductBean() {
    }

    /**
     * Constructs a ProductBean with essential fields.
     *
     * @param productId     Unique product identifier
     * @param accountId     Associated merchant account identifier
     * @param currencyId    Associated currency identifier
     * @param discount      Applied discount
     * @param description   Product description
     * @param isActive      Active status flag
     * @param price         Product price
     * @param productName   Display name of the product
     * @param stockQuantity Available stock
     */
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
        this.createdAt = LocalDateTime.now(); // Safe default timestamp for early instantiation
    }

    /**
     * Constructs a fully-initialized ProductBean with all analytical and metadata fields.
     *
     * @param productId      Unique product identifier
     * @param accountId      Associated merchant account identifier
     * @param currencyId     Associated currency identifier
     * @param averageRating  Calculated average rating
     * @param createdAt      Publication timestamp
     * @param discount       Applied discount
     * @param description    Product description
     * @param isActive       Active status flag
     * @param latestUpdate   Latest update timestamp
     * @param price          Product price
     * @param productName    Display name of the product
     * @param stockQuantity  Available stock
     * @param totalDownloads Total download metrics
     * @param totalReviews   Total reviews count
     */
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

    // --- Getters and Setters ---

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

    public void setActive(boolean isActive) {
        this.isActive = isActive; // Added explicit 'this' reference for stylistic consistency
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

    // --- Standard Object Override Methods ---

    /**
     * Compares this product with another object for equality.
     * Equality is determined strictly by the unique productId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same productId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductBean that = (ProductBean) o;
        return Objects.equals(productId, that.productId);
    }

    /**
     * Generates a hash code based on the unique productId.
     *
     * @return A hash code value for this product bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    /**
     * Returns a string representation of the ProductBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "ProductBean{" + // Fixed class name consistency
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
