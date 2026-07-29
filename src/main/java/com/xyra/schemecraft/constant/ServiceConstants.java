package com.xyra.schemecraft.constant;

import java.util.Set;

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
     * Allowed file extensions for product gallery images.
     */
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".webp");

    /**
     * Allowed HTTP content types for product gallery images, used as a secondary
     * validation layer alongside extension checks.
     */
    public static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp");

    /**
     * Maximum allowed size, in bytes, for a single product gallery image upload.
     */
    public static final long MAX_IMAGE_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB

    /**
     * Allowed file extensions for product version schematic files.
     */
    public static final Set<String> ALLOWED_SCHEMATIC_EXTENSIONS = Set.of(".schematic", ".schem", ".litematic");

    /**
     * Maximum allowed size, in bytes, for a single product version schematic upload.
     */
    public static final long MAX_SCHEMATIC_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB

    /**
     * Private constructor to prevent instantiation of utility class.
     *
     * @throws AssertionError if instantiation is attempted
     */
    private ServiceConstants() {
        throw new AssertionError("ServiceConstants cannot be instantiated");
    }
}
