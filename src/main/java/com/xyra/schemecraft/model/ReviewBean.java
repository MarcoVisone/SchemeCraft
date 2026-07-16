package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Represents the Review domain model and data transfer object within the application.
 */
public class ReviewBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the associated Account that wrote the review. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Unique identifier of the associated Product being reviewed. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /** Textual feedback comment provided by the user. */
    private String comment;

    /** Timestamp indicating exactly when the review was submitted. */
    private LocalDateTime createdAt;

    /** Flag indicating if the system verified that the account actually purchased the product before reviewing. */
    private boolean isVerifiedPurchase;

    /** Numerical rating score. */
    @Min(value = 1, message = "Rating must be at least {value}")
    @Max(value = 5, message = "Rating cannot exceed {value}")
    private int rating;

    /**
     * Default no-argument constructor.
     */
    public ReviewBean() {
    }

    /**
     * Constructs a ReviewBean with standard user input fields.
     *
     * @param accountId          Associated reviewer account identifier
     * @param productId          Associated reviewed product identifier
     * @param comment            Textual feedback comment
     * @param isVerifiedPurchase Purchase verification status flag
     * @param rating             Numerical rating score (1-5)
     */
    public ReviewBean(String accountId, String productId, String comment, boolean isVerifiedPurchase, int rating) {
        this.accountId = accountId;
        this.productId = productId;
        this.comment = comment;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.rating = rating;
        this.createdAt = LocalDateTime.now(); // Safe default timestamp
    }

    /**
     * Constructs a fully-initialized ReviewBean.
     *
     * @param accountId          Associated reviewer account identifier
     * @param productId          Associated reviewed product identifier
     * @param comment            Textual feedback comment
     * @param createdAt          Submission timestamp
     * @param isVerifiedPurchase Purchase verification status flag
     * @param rating             Numerical rating score (1-5)
     */
    public ReviewBean(String accountId, String productId, String comment, LocalDateTime createdAt,
                      boolean isVerifiedPurchase, int rating) {
        this.accountId = accountId;
        this.productId = productId;
        this.comment = comment;
        this.createdAt = createdAt;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.rating = rating;
    }

    // --- Getters and Setters ---

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
        this.isVerifiedPurchase = verifiedPurchase;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this review with another object for equality.
     * Equality is determined by the composite relation: both accountId and productId must match.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same accountId and productId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewBean that = (ReviewBean) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(productId, that.productId);
    }

    /**
     * Generates a hash code based on the composite identifiers (accountId and productId).
     *
     * @return A hash code value for this review bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountId, productId);
    }

    /**
     * Returns a string representation of the ReviewBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "ReviewBean{" + // Fixed class name consistency
                "accountId='" + accountId + '\'' +
                ", productId='" + productId + '\'' +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", isVerifiedPurchase=" + isVerifiedPurchase +
                ", rating=" + rating +
                '}';
    }
}
