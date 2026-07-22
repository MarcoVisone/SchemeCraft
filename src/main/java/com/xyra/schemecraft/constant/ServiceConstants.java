package com.xyra.schemecraft.constant;

/**
 * Utility class containing service-layer business rules, limits, and pagination parameters.
 */
public final class ServiceConstants {

    /**
     * Default page size for paginated order history listings.
     */
    public static final int ORDERS_PAGE_SIZE = 10;

    /**
     * Default page size for paginated product review listings.
     */
    public static final int REVIEWS_PAGE_SIZE = 10;

    /**
     * Maximum allowed number of images per product.
     */
    public static final int MAX_PRODUCT_IMAGES = 10;

    /**
     * Private constructor to prevent instantiation of utility class.
     *
     * @throws AssertionError if instantiation is attempted
     */
    private ServiceConstants() {
        throw new AssertionError("ServiceConstants cannot be instantiated");
    }
}
