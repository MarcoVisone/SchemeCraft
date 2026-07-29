package com.xyra.schemecraft.dto;

import java.math.BigDecimal;

import com.xyra.schemecraft.model.OrderItemBean;
import com.xyra.schemecraft.model.ProductBean;

/**
 * Data Transfer Object (DTO) aggregating individual order item transaction data
 * with its associated product domain entity, providing calculated total utilities.
 */
public class OrderItemDetailDTO {

    /** Granular order item record containing pricing, quantity, and tax data. */
    private OrderItemBean item;

    /** Product entity associated with the line item. */
    private ProductBean product;

    /**
     * Default no-argument constructor.
     */
    public OrderItemDetailDTO() {
    }

    /**
     * Constructs a fully-initialized OrderItemDetailDTO.
     *
     * @param item    Order item domain entity containing transactional details
     * @param product Associated product domain model
     */
    public OrderItemDetailDTO(OrderItemBean item, ProductBean product) {
        this.item = item;
        this.product = product;
    }

    // --- Getters and Setters ---

    public OrderItemBean getItem() {
        return item;
    }

    public void setItem(OrderItemBean item) {
        this.item = item;
    }

    public ProductBean getProduct() {
        return product;
    }

    public void setProduct(ProductBean product) {
        this.product = product;
    }

    // --- Business / Calculation Methods ---

    /**
     * Computes the net total price for this line item by applying discounts and taxes to the base price.
     * Formula: (Base Price - Discount) + Tax
     *
     * @return Calculated line total as a {@link BigDecimal}, or {@link BigDecimal#ZERO} if item or price is null
     */
    public BigDecimal getLineTotal() {
        if (item == null || item.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = item.getPrice();
        if (item.getDiscount() != null) {
            total = total.subtract(item.getDiscount());
        }
        if (item.getTax() != null) {
            total = total.add(item.getTax());
        }
        return total;
    }
}
