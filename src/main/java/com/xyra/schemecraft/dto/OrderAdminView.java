package com.xyra.schemecraft.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only Data Transfer Object representing a denormalized order row for the admin
 * order listing table. Combines order data with the associated customer's identifying
 * information (username, email) obtained via a join, so the admin view does not need
 * to perform separate lookups per row.
 * <p>
 * This is a view object only: it is never persisted or updated, so it is immutable
 * and exposes no setters.
 */
public class OrderAdminView {

    private final String orderId;
    private final LocalDateTime createdAt;
    private final BigDecimal totalAmount;
    private final int status;
    private final String accountId;
    private final String username;
    private final String email;

    public OrderAdminView(String orderId, LocalDateTime createdAt, BigDecimal totalAmount, int status,
                          String accountId, String username, String email) {
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.status = status;
        this.accountId = accountId;
        this.username = username;
        this.email = email;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public int getStatus() {
        return status;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "OrderAdminView{" +
                "orderId='" + orderId + '\'' +
                ", createdAt=" + createdAt +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
