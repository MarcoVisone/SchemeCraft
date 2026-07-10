package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String accountId;
    private String addressId;
    private String currencyId;
    private int methodType;
    private int status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private String transactionId;

    public Order() {
    }

    public Order(String orderId, String accountId, String addressId, String currencyId, int methodType, int status,
                 LocalDateTime createdAt, BigDecimal totalAmount, String transactionId) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.addressId = addressId;
        this.currencyId = currencyId;
        this.methodType = methodType;
        this.status = status;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.transactionId = transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public int getMethodType() {
        return methodType;
    }

    public void setMethodType(int methodType) {
        this.methodType = methodType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", addressId='" + addressId + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", methodType=" + methodType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", totalAmount=" + totalAmount +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
