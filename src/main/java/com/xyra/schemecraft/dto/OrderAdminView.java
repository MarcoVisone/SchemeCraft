package com.xyra.schemecraft.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing an aggregated view of an order
 * designed for administrative dashboards, combining transaction metrics and customer details.
 *
 * @param orderId     Unique identifier assigned to the order
 * @param createdAt   Timestamp recording when the order was placed
 * @param totalAmount Total monetary valuation of the order
 * @param status      Numerical representation of the current order status
 * @param accountId   Unique identifier of the account that placed the order
 * @param username    Display name of the account holder
 * @param email       Primary email address linked to the customer account
 */
public record OrderAdminView(

        String orderId,

        LocalDateTime createdAt,

        BigDecimal totalAmount,

        int status,

        String accountId,

        String username,

        String email
) {
    /**
     * Compact constructor performing parameter assertion and string sanitization.
     *
     * @throws IllegalArgumentException if orderId, createdAt, totalAmount, or accountId is null
     */
    public OrderAdminView {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Creation timestamp cannot be null");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("Total amount cannot be null");
        }
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }

        orderId = orderId.trim();
        accountId = accountId.trim();
        username = username != null ? username.trim() : null;
        email = email != null ? email.trim() : null;
    }
}
