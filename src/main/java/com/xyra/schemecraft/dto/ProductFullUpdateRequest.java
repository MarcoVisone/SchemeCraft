package com.xyra.schemecraft.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) for the complete update of an existing product.
 * Aggregates all data that can be modified in a single atomic operation:
 * basic product data, categories, images, and versions.
 *
 * @param productId      ID of the product to update (required)
 * @param product        Basic product data (required)
 * @param categoryIds    Final list of category IDs to associate (complete sync)
 * @param imagePaths     Final list of image paths, in display order
 * @param versions       List of versions to keep (created, updated, or deleted)
 */
public record ProductFullUpdateRequest(

        String productId,

        ProductRequest product,

        List<String> categoryIds,

        List<String> imagePaths,

        List<VersionUpdateRequest> versions
) {
    /**
     * Compact constructor performing mandatory constraint assertions and defensive copying.
     *
     * @throws IllegalArgumentException if productId or product are null/empty,
     *                                  or if categoryIds/imagePaths/versions are null
     */
    public ProductFullUpdateRequest {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }
        if (categoryIds == null) {
            throw new IllegalArgumentException("Category IDs list cannot be null (use empty list if none)");
        }
        if (imagePaths == null) {
            throw new IllegalArgumentException("Image paths list cannot be null (use empty list if none)");
        }
        if (versions == null) {
            throw new IllegalArgumentException("Versions list cannot be null (use empty list if none)");
        }

        categoryIds = List.copyOf(categoryIds);
        imagePaths = List.copyOf(imagePaths);
        versions = List.copyOf(versions);
    }
}
