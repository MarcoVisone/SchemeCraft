package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

/**
 * Represents the Order domain model and data transfer object within the application.
 */
public class OrderBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the transaction order. */
    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;

    /** Reference identifier of the Account that placed the order. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Reference identifier of the Address associated with this order. */
    @NotBlank(message = "Address ID cannot be blank")
    private String addressId;

    /** Reference identifier of the Currency used for the payment. */
    @NotBlank(message = "Currency ID cannot be blank")
    private String currencyId;

    /** Numeric code representing the payment method type. */
    private int methodType;

    /** Numeric code representing the current order processing status. */
    private int status;

    /** Timestamp indicating exactly when the order was placed. */
    private LocalDateTime createdAt;

    /** Total monetary amount of the order, including taxes. */
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    /** Unique gateway transaction ID for tracking external payments. */
    @NotBlank(message = "Transaction ID cannot be blank")
    private String transactionId;

    /**
     * Default no-argument constructor.
     */
    public OrderBean() {
    }

    /**
     * Constructs an OrderBean without creation timestamp and total amount.
     *
     * @param orderId       Unique order identifier
     * @param accountId     Associated purchaser account identifier
     * @param addressId     Associated delivery/billing address identifier
     * @param currencyId    Associated currency identifier
     * @param methodType    Payment method type code
     * @param status        Initial order status code
     * @param transactionId Processor transaction reference ID
     */
    public OrderBean(String orderId, String accountId, String addressId, String currencyId, int methodType,
                     int status, String transactionId) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.addressId = addressId;
        this.currencyId = currencyId;
        this.methodType = methodType;
        this.status = status;
        this.transactionId = transactionId;
    }

    /**
     * Constructs a fully-initialized OrderBean containing all transactional fields.
     *
     * @param orderId       Unique order identifier
     * @param accountId     Associated purchaser account identifier
     * @param addressId     Associated delivery/billing address identifier
     * @param currencyId    Associated currency identifier
     * @param methodType    Payment method type code
     * @param status        Order status code
     * @param createdAt     Timestamp when the order was finalized
     * @param totalAmount   Total transaction amount
     * @param transactionId Processor transaction reference ID
     */
    public OrderBean(String orderId, String accountId, String addressId, String currencyId, int methodType, int status,
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

    // --- Getters and Setters ---

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

    // --- Standard Object Override Methods ---

    /**
     * Compares this order with another object for equality.
     * Equality is determined strictly by the unique orderId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same orderId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderBean orderBean = (OrderBean) o;
        return Objects.equals(orderId, orderBean.orderId);
    }

    /**
     * Generates a hash code based on the unique orderId.
     *
     * @return A hash code value for this order bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    /**
     * Returns a string representation of the OrderBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "OrderBean{" + // Fixed class name consistency
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
