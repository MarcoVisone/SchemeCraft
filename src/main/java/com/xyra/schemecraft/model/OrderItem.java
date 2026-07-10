package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String productId;
    private BigDecimal discount;
    private BigDecimal price;
    private BigDecimal tax;

    public OrderItem() {
    }

    public OrderItem(String orderId, String productId, BigDecimal discount, BigDecimal price, BigDecimal tax) {
        this.orderId = orderId;
        this.productId = productId;
        this.discount = discount;
        this.price = price;
        this.tax = tax;
    }

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

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", discount=" + discount +
                ", price=" + price +
                ", tax=" + tax +
                '}';
    }
}
