package com.xyra.schemecraft.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) aggregating everything required to create a fully published product
 * in a single atomic operation: base product data, category assignments, gallery images,
 * and the first downloadable version.
 * <p>
 * The embedded {@link ProductVersionRequest} is expected to have a null or empty productId,
 * since the product does not exist yet at the time this request is built by the caller.
 * The consuming service is responsible for generating the product ID and resolving it
 * into the version request before persisting it.
 *
 * @param productRequest Base product data (name, price, currency, stock, etc.)
 * @param categoryIds    List of category IDs to associate with the product (at least one required)
 * @param imagePaths     List of image paths for the product gallery, in display order (at least one required)
 * @param versionRequest Data for the first product version release (productId is ignored if present)
 */
public record ProductFullRequest(
        ProductRequest productRequest,
        List<String> categoryIds,
        List<String> imagePaths,
        ProductVersionRequest versionRequest
) {
    /**
     * Compact constructor performing mandatory constraint assertions.
     *
     * @throws IllegalArgumentException if any mandatory field is missing or empty
     */
    public ProductFullRequest {
        if (productRequest == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("At least one category ID is required");
        }
        if (imagePaths == null || imagePaths.isEmpty()) {
            throw new IllegalArgumentException("At least one image path is required");
        }
        if (versionRequest == null) {
            throw new IllegalArgumentException("Version request cannot be null");
        }

        categoryIds = List.copyOf(categoryIds);
        imagePaths = List.copyOf(imagePaths);
    }
}
