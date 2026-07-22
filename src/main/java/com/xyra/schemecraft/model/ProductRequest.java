package com.xyra.schemecraft.model;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) carrying the payload required to create or update a product entry.
 *
 * @param accountId      Unique identifier of the seller or creator account
 * @param currencyId     Unique identifier of the currency associated with pricing
 * @param productName    Display title or name of the product
 * @param description    Detailed textual summary or features of the product
 * @param discount       Discount percentage or amount applicable to the product
 * @param price          Base monetary price of the product
 * @param stockQuantity  Available inventory units count (ignored if unlimitedStock is true)
 * @param unlimitedStock Flag indicating whether the product has unlimited digital/physical inventory
 */
public record ProductRequest(
        String accountId,
        String currencyId,
        String productName,
        String description,
        BigDecimal discount,
        BigDecimal price,
        Integer stockQuantity,
        boolean unlimitedStock
) {
    /**
     * Compact constructor performing parameter sanitization and mandatory constraint assertions.
     *
     * @throws IllegalArgumentException if mandatory fields are missing or numeric values are invalid
     */
    public ProductRequest {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (discount != null && discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }

        accountId = accountId.trim();
        currencyId = currencyId.trim();
        productName = productName.trim();
        description = description != null ? description.trim() : null;

        if (unlimitedStock) {
            stockQuantity = 0;
        } else if (stockQuantity == null || stockQuantity < 0) {
            stockQuantity = 0;
        }
    }
}
