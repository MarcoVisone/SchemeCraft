package com.xyra.schemecraft.dto;

import java.time.LocalDateTime;

import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ReviewBean;

/**
 * Wrapper object combining a ReviewBean with its Author Account details for UI rendering.
 *
 * @param review The review domain model entity
 * @param author The account profile entity of the review author
 */
public record ReviewView(

        ReviewBean review,

        AccountBean author
) {

    // --- Delegated Getter Methods ---

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

    public String getAuthorUsername() {
        return (author != null && author.getUsername() != null) ? author.getUsername() : "Anonymous";
    }

    public String getAuthorAvatar() {
        return (author != null) ? author.getProfileImagePath() : null;
    }
}
