package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, ProductBean> products;

    public Cart() {
        this.products = new HashMap<>();
    }

    public Map<String, ProductBean> getProducts() {
        return products;
    }

    public void addProduct(ProductBean product) {
        if (product != null && product.getProductId() != null) {
            this.products.put(product.getProductId(), product);
        }
    }

    public void removeProduct(String productId) {
        if (productId != null) {
            this.products.remove(productId);
        }
    }

    public Collection<ProductBean> getItems() {
        return this.products.values();
    }
    public BigDecimal getTotalAmount() {
        return this.products.values().stream()
                .map(product -> {
                    BigDecimal price = product.getPrice();
                    BigDecimal discountPct = product.getDiscount();

                    if (discountPct != null && discountPct.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal discountAmount = price.multiply(discountPct)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        return price.subtract(discountAmount);
                    }
                    return price;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void clear() {
        this.products.clear();
    }

    @Override
    public String toString() {
        return "Cart{productsCount=" + products.size() + '}';
    }
}
