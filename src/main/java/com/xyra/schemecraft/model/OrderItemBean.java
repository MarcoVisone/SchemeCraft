package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

/**
 * Represents the OrderItem domain model and data transfer object within the application.
 */
public class OrderItemBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the associated parent Order. */
    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;

    /** Unique identifier of the purchased Product. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /** Discount applied to this item at the time of purchase. */
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discount;

    /** Unit price of the product at the time of purchase, before taxes and discounts. */
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal price;

    /** Tax rate or tax amount applied to this specific item. */
    @DecimalMin(value = "0.0", message = "Tax cannot be negative")
    private BigDecimal tax;

    /**
     * Default no-argument constructor.
     */
    public OrderItemBean() {
    }

    /**
     * Constructs a fully-initialized OrderItemBean with snapshot financial details.
     *
     * @param orderId   Associated order identifier
     * @param productId Associated product identifier
     * @param discount  Snapshot discount applied
     * @param price     Snapshot unit price
     * @param tax       Snapshot tax applied
     */
    public OrderItemBean(String orderId, String productId, BigDecimal discount, BigDecimal price, BigDecimal tax) {
        this.orderId = orderId;
        this.productId = productId;
        this.discount = discount;
        this.price = price;
        this.tax = tax;
    }

    // --- Getters and Setters ---

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this order item with another object for equality.
     * Equality is determined strictly by its composite relationship keys: orderId and productId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same orderId and productId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemBean that = (OrderItemBean) o;
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(productId, that.productId);
    }

    /**
     * Generates a hash code based on the composite identifiers (orderId and productId).
     *
     * @return A hash code value for this order item bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(orderId, productId);
    }

    /**
     * Returns a string representation of the OrderItemBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "OrderItemBean{" + // Fixed class name consistency
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", discount=" + discount +
                ", price=" + price +
                ", tax=" + tax +
                '}';
    }
}
