package com.xyra.schemecraft.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) carrying the payload required to submit or update a product review.
 *
 * @param accountId Unique identifier of the review author account
 * @param productId Unique identifier of the product being reviewed
 * @param rating    Numerical score assigned to the product, ranging from 1 to 5
 * @param comment   Detailed textual feedback or opinion regarding the product
 */
public record ReviewRequest(

        @NotBlank(message = "Account ID cannot be blank")
        String accountId,

        @NotBlank(message = "Product ID cannot be blank")
        String productId,

        @Min(value = 1, message = "Rating must be at least {value}")
        @Max(value = 5, message = "Rating cannot exceed {value}")
        int rating,

        String comment
) {
        /**
         * Compact constructor performing parameter sanitization and mandatory constraint assertions.
         *
         * @throws IllegalArgumentException if Account ID or Product ID are null/empty, or if rating is out of bounds (1-5)
         */
        public ReviewRequest {
                if (accountId == null || accountId.trim().isEmpty()) {
                        throw new IllegalArgumentException("Account ID cannot be null or empty for a review request");
                }
                if (productId == null || productId.trim().isEmpty()) {
                        throw new IllegalArgumentException("Product ID cannot be null or empty for a review request");
                }

            accountId = accountId.trim();
                productId = productId.trim();
                comment = comment != null ? comment.trim() : null;
        }
}
