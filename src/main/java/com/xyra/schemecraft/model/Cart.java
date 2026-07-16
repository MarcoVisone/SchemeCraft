package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a shopping cart in the SchemeCraft application.
 */
public class Cart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Constant representing 100 for percentage calculations. */
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    /** Internal store of products indexed by their unique product identifier. */
    private final Map<String, ProductBean> products;

    /**
     * Constructs a new, empty shopping Cart.
     */
    public Cart() {
        this.products = new HashMap<>();
    }

    /**
     * Returns an unmodifiable view of the products map.
     *
     * @return An unmodifiable Map containing product IDs as keys and ProductBean instances as values.
     */
    public Map<String, ProductBean> getProducts() {
        return Collections.unmodifiableMap(products);
    }

    /**
     * Adds a product to the cart. If the product already exists, it is overwritten.
     *
     * @param product The product to be added
     */
    public void addProduct(ProductBean product) {
        if (product != null && product.getProductId() != null) {
            this.products.put(product.getProductId(), product);
        }
    }

    /**
     * Removes a product from the cart by its unique identifier.
     *
     * @param productId The unique ID of the product to remove
     */
    public void removeProduct(String productId) {
        if (productId != null) {
            this.products.remove(productId);
        }
    }

    /**
     * Returns an unmodifiable collection of all products currently in the cart.
     *
     * @return An unmodifiable Collection of ProductBeans
     */
    public Collection<ProductBean> getItems() {
        return Collections.unmodifiableCollection(this.products.values());
    }

    /**
     * Calculates the total price of all items in the cart, taking individual product
     * discounts into account.
     *
     * @return The total BigDecimal amount of the cart, guaranteed non-null and scaled to 2 decimal places.
     */
    public BigDecimal getTotalAmount() {
        return this.products.values().stream()
                .map(this::calculateDiscountedPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Resets the cart by removing all products.
     */
    public void clear() {
        this.products.clear();
    }

    /**
     * Helper method to safely calculate the discounted price of a single product.
     * If the price is null, it defaults to zero to prevent NullPointerException.
     *
     * @param product The target product
     * @return The final net price after discount
     */
    private BigDecimal calculateDiscountedPrice(ProductBean product) {
        BigDecimal price = product.getPrice();
        if (price == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountPct = product.getDiscount();
        if (discountPct != null && discountPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = price.multiply(discountPct)
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            return price.subtract(discountAmount);
        }

        return price;
    }

    // --- Standard Object Override Methods ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(products, cart.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "productsCount=" + products.size() +
                ", totalAmount=" + getTotalAmount() +
                '}';
    }
}
