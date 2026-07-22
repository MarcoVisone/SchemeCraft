package com.xyra.schemecraft.dao;

/**
 * Data Transfer Object (DTO) carrying the payload required to create or register a new product version release.
 *
 * @param productId        Unique identifier of the associated product
 * @param changelog        Detailed summary of changes, fixes, and features included in this release
 * @param filePath         Storage file path or key referencing the binary artifact
 * @param minecraftVersion Target Minecraft game version compatibility string
 * @param version          Semantic release version number
 */
public record ProductVersionRequest(
        String productId,
        String changelog,
        String filePath,
        String minecraftVersion,
        String version
) {
    /**
     * Compact constructor performing parameter sanitization and validation.
     *
     * @throws IllegalArgumentException if mandatory fields are null or empty
     */
    public ProductVersionRequest {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        productId = productId.trim();
        changelog = changelog != null ? changelog.trim() : null;
        filePath = filePath.trim();
        minecraftVersion = minecraftVersion != null ? minecraftVersion.trim() : null;
        version = version.trim();
    }
}
