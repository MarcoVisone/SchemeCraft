package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ReviewBean;

import java.time.LocalDateTime;

/**
 * Wrapper object combining a ReviewBean with its Author Account details for UI rendering.
 */
public class ReviewView {

    private final ReviewBean review;
    private final AccountBean author;

    public ReviewView(ReviewBean review, AccountBean author) {
        this.review = review;
        this.author = author;
    }

    public ReviewBean getReview() {
        return review;
    }

    public AccountBean getAuthor() {
        return author;
    }

    // --- Delegated Review Properties ---
    public String getAccountId() {
        return review != null ? review.getAccountId() : null;
    }

    public String getProductId() {
        return review != null ? review.getProductId() : null;
    }

    public String getComment() {
        return review != null ? review.getComment() : null;
    }

    public LocalDateTime getCreatedAt() {
        return review != null ? review.getCreatedAt() : null;
    }

    public boolean isVerifiedPurchase() {
        return review != null && review.isVerifiedPurchase();
    }

    public boolean getIsVerifiedPurchase() {
        return review != null && review.isVerifiedPurchase();
    }

    public boolean getVerifiedPurchase() {
        return review != null && review.isVerifiedPurchase();
    }

    public int getRating() {
        return review != null ? review.getRating() : 0;
    }

    // --- Author Properties accessed by JSP (${rev.authorUsername}, ${rev.authorAvatar}) ---
    public String getAuthorUsername() {
        return (author != null && author.getUsername() != null) ? author.getUsername() : "Anonymous";
    }

    public String getAuthorAvatar() {
        return (author != null) ? author.getProfileImagePath() : null;
    }
}
