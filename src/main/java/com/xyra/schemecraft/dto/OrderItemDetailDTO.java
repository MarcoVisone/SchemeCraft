package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.OrderItemBean;
import com.xyra.schemecraft.model.ProductBean;
import java.math.BigDecimal;

public class OrderItemDetailDTO {

    private OrderItemBean item;
    private ProductBean product;

    public OrderItemDetailDTO() {
    }

    public OrderItemDetailDTO(OrderItemBean item, ProductBean product) {
        this.item = item;
        this.product = product;
    }

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

    /**
     * Calcola il totale pagato per questa singola riga di prodotto: (Prezzo - Sconto) + Tassa
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
